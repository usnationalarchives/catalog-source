package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import java.io.File;
import java.io.IOException;

public class WriteObjectsXmlStage extends IngestionStage {
   
  @Override
  public void process(Job job) throws AspireException {    
    JobInfo jobInfo = Jobs.getJobInfo(job);
    if (jobInfo.isProcessingAnnotations()){
      return;
    } 
    
    try{
      save(job, jobInfo);
    } catch(Throwable e){
      throw new AspireException("save objects xml", e);
    }
  }
  
  private void save(Job job, JobInfo jobInfo) throws AspireException, IOException {
    AspireObject doc = job.get();
    AspireObject objects = doc.get(ObjectsXml.OBJECTS_XML_TOP_LEVEL_ELEMENT);

    if (objects == null){
      return;
    }

    File objectsFile = File.createTempFile("objects", ".xml");

    objects.writeXml(objectsFile, AspireObject.PRETTY + AspireObject.XML_PREFIX);

    OpaStorage opaStorage = jobInfo.getOpaStorage();

    String pathToObjectsXml = jobInfo.getPathToObjectsXml();

    boolean fileHasChanged = !Digests.md5Hex(objectsFile).equals(opaStorage.md5Hex(pathToObjectsXml));

    if (fileHasChanged){
      opaStorage.saveFile(objectsFile, pathToObjectsXml);
    }

    objectsFile.delete();

    // delete the objects.xml file if it's in the deprecated "TO4" area
    String deprecatedPath = opaStorage.getFullNaIdPathInLive(opaStorage.OBJECTS_XML);
    if ( opaStorage.exists(deprecatedPath) ) {
      opaStorage.deleteFiles(deprecatedPath);
    }
  }
}
