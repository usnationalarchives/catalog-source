package gov.nara.opa.ingestion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.searchtechnologies.aspire.framework.Standards;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;

import java.io.*;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.w3c.dom.Element;

public class SetRecordStage extends IngestionStage {
  static final String DELETES_TAG = "deletes";
  static final String NAID_TAG = "naId";
  
  private OpaStorageFactory opaStorageFactory;
  private Settings settings;
  
  @Override
  public void initialize(Element config) throws AspireException {
    opaStorageFactory = Components.getOpaStorageFactory(this);
    settings = Components.getSettings(this);
  }
  
  @Override
  public void process(Job job) throws AspireException {   
    JobInfo jobInfo = Jobs.getJobInfo(job);
    jobInfo.setObjectsXmlLock(new ReentrantLock());
    jobInfo.setExtractTextEnabled(settings.isExtractTextEnabled());
    jobInfo.setForcedTextExtract(settings.isForcedTextExtract());

    setRecord(job, jobInfo);

    AspireObject record = jobInfo.getRecord();

    if (isDeletesRecord(record) && !settings.isReindex()){
      try {
        processDeletes(record, job);
      } catch (Exception e) {
        throw new AspireException(e.getMessage(),e);
      } finally {
        job.terminate();
      }
      return;
    } 

    setRecordType(jobInfo);

    if (!isSupportedRecordType(jobInfo.getRecordType())){
      String url = job.get().getText(Standards.Basic.FETCH_URL_TAG);
      warn("Unsupported record type '%s' in record %s", jobInfo.getRecordType(), url);
      job.terminate();
      return;
    }

    setNAID(jobInfo);

    setOpaStorage(jobInfo);

    setDescription(jobInfo);

    setDigitalObjectMap(jobInfo);

    if(jobInfo.getRecord().get("digitalObjectArray") != null &&
            jobInfo.getRecord().get("digitalObjectArray").hasChildren()) {
      setPreviousObjectsXml(jobInfo);
    }
  }
  
  private AspireObject loadRecordFromXmlStore(JobInfo jobInfo) throws AspireException {
    Integer naid = jobInfo.getNAID();
    jobInfo.setOpaStorage(opaStorageFactory.createOpaStorage(naid));

    AspireObject record = jobInfo.getRecordInXmlStore();
    if (record == null){
      throw new AspireException("Not found in xmlstore", "Record %d not found in xmlstore", naid);      
    }    
    return record;
  }

  private boolean isDeletesRecord(AspireObject record){
    return DELETES_TAG.equals(record.getName());
  }
  
  AspireObject loadRecord(JobInfo info) throws AspireException{    
    try (InputStream stream = info.createInputStream(); 
        Reader reader = new BufferedReader(new InputStreamReader(stream))){      
      return AspireObject.createFromXML(reader);
    } catch (Throwable ex) {
      throw new AspireException("create aspire object", ex);
    }    
  }

  private void processDeletes(AspireObject record, Job job) throws AspireException, SQLException {
    
    for (AspireObject child : record.getChildren(true)){
      if (NAID_TAG.equals(child.getName())){
        String naidString = child.getText();
        Integer naid = Integer.parseInt(naidString);
        RecordRemover recordRemover = new RecordRemover(this, opaStorageFactory, naid, settings.getDbConnection(), job);
        recordRemover.execute();
      }
    }
  }

  private void setRecordType(JobInfo jobInfo) throws AspireException {
    AspireObject record = jobInfo.getRecord();
    String recordType = record.getName();
    jobInfo.setRecordType(recordType);
  }

  private boolean isSupportedRecordType(String recordType){
    return Records.RECORD_TAGS.contains(recordType);
  }

  private void setNAID(JobInfo jobInfo) throws AspireException {
    if (jobInfo.isProcessingAnnotations()){
      return;
    }    
    
    AspireObject record = jobInfo.getRecord();
    String naid = record.getText(NAID_TAG);
    boolean isValidNAID = Integers.tryParse(naid);
    
    if (!isValidNAID){
      throw new AspireException("invalid naId", 
        "Invalid naId %s in file %s", naid, jobInfo.getFile()
      );
    }
    
    jobInfo.setNAID(naid);
  }

  private void setDigitalObjectMap(JobInfo jobInfo) throws AspireException {    
    AspireObject digitalObjectArray = jobInfo.getRecord().get("digitalObjectArray");
    if (digitalObjectArray != null){
      Map<String, AspireObject> digitalObjects = jobInfo.createDigitalObjects();      
      for (AspireObject child : digitalObjectArray.getChildren()){
        if ("digitalObject".equals(child.getName())){
          digitalObjects.put(child.getText("objectIdentifier"), child);
        }
      }
    }
  }  

  private void setDescription(JobInfo jobInfo) {
    jobInfo.setDescription(
      String.format("%s %d", jobInfo.getRecordType(), jobInfo.getNAID())
    );
  }

  private void setOpaStorage(JobInfo jobInfo) {
    if (jobInfo.isProcessingAnnotations()){
      return;
    }
    
    jobInfo.setOpaStorage(
      opaStorageFactory.createOpaStorage(
        jobInfo.getNAID()
      )
    );
  }

  private void setRecord(Job job, JobInfo jobInfo) throws AspireException {
    if (jobInfo.isProcessingAnnotations()){
        AspireObject record = loadRecordFromXmlStore(jobInfo);
        jobInfo.setRecord(record);
        job.get().set(record);
        jobInfo.setRecordFromXmlStore(record);
    }
  }

  private void setPreviousObjectsXml(JobInfo jobInfo) throws AspireException {
    AspireObject previousObjects = null;

    if (settings.isReindex() && settings.getObjectsFromSolr()) {
      // check Solr for objects XML first...
      debug("Getting objects XML from Solr for NAID: "+jobInfo.getNAID());
      previousObjects = getSolrObjectXML(jobInfo.getNAID());
    }

    // if reindexing and not found in Solr.  or any other time...
    if (previousObjects == null) {
      // check S3 for objects.xml to update technical metadata that was previously processed
      OpaStorage opaStorage = jobInfo.getOpaStorage();
      String objectsXmlKey = jobInfo.getPathToObjectsXml();
      String deprecatedObjectXMLKey = opaStorage.getFullNaIdPathInLive(opaStorage.OBJECTS_XML);

      // first try expected location
      File objectsXmlFile = opaStorage.getFile(objectsXmlKey);

      boolean isFromDeprecatedArea = false;

      // if the file is null, try the deprecated "TO4" location...
      if (objectsXmlFile == null) {
        debug("objects.xml not found at " + objectsXmlKey + ".  Checking " + deprecatedObjectXMLKey + "  NAID: " + jobInfo.getNAID());
        objectsXmlFile = opaStorage.getFile(deprecatedObjectXMLKey);
        isFromDeprecatedArea = true;
      }

      if (objectsXmlFile != null) {
        debug("objects.xml found in S3 for NAID " + jobInfo.getNAID());
        try {
          String objectsXml = new String(Files.readAllBytes(objectsXmlFile.toPath()));
          AspireObject tmpObject = new AspireObject("tmp");
          tmpObject.loadXML(new StringReader(objectsXml));
          previousObjects = tmpObject.get("objects");
          debug("NAID: %s previousObject: %s",jobInfo.getNAID(),previousObjects.toXmlString(true));
          if (isFromDeprecatedArea) {
            // now move the objects.xml file from the deprecated location to the "old"
            // location.  This is a little unnecessary because the objects.xml file will ultimately
            // get written to the "old" location at the end of the processing chain. just want to make
            // sure it gets there somehow.
            try {
              opaStorage.copyFileFromS3(Components.getSettings(this).getS3StorageBucketName(), deprecatedObjectXMLKey, objectsXmlKey);
              opaStorage.deleteFiles(deprecatedObjectXMLKey);
            } catch (IOException e) {
              e.printStackTrace();
              error("Error moving " + jobInfo.getNAID() + " objects.xml from: " + deprecatedObjectXMLKey + " to " + objectsXmlKey, e);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          error("Error reading previously generated objects.xml for NAID: " + jobInfo.getNAID(), e);
        }
        objectsXmlFile.delete();
      }
    }

    jobInfo.setPreviousObjectsXML(previousObjects);
  }

  private AspireObject getSolrObjectXML(int naid) throws AspireException {
    SolrServer solrServer = new SolrServer(this);
    String solrResponse = solrServer.getRecord(naid);
    if (solrResponse != null) {
      JsonObject responseJson = new Gson().fromJson(solrResponse, JsonObject.class);
      JsonObject response = responseJson.getAsJsonObject("response");
      int resultCount = response.get("numFound").getAsInt();
      boolean hasObjectsInSolrRecord = true; // assume objects are there (we already checked DAS record for objects...)
      if (resultCount == 1) {
        String objectsXml = null;
        try {
          objectsXml = response.getAsJsonArray("docs").get(0).getAsJsonObject().get("objects").getAsString();
        } catch (NullPointerException e) {
          info("No object XML found in Solr result for NAID: " + naid);
          hasObjectsInSolrRecord = false;
        }
        if (objectsXml != null || hasObjectsInSolrRecord) {
          info("Using objects XML from Solr for NAID: " + naid);
          AspireObject tmpObject = new AspireObject("tmp");
          tmpObject.loadXML(new StringReader(objectsXml));
          return tmpObject.get("objects");
        }
      } else if (resultCount == 0) {
        info("No record found in Solr for NAID: " + naid);
      } else if (resultCount > 1) {
        info("More than one record found in Solr for NAID: " + naid);
      }
    }
    return null;
  }
}
