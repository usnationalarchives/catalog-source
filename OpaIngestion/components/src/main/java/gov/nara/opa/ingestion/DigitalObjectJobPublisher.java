package gov.nara.opa.ingestion;

import java.util.Map;

import com.searchtechnologies.aspire.framework.JobFactory;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.BranchHandler;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;

public class DigitalObjectJobPublisher {
  private final BranchHandler branchHandler;
  private final ALogger logger;
  private final Job job;
  private final JobInfo jobInfo;
  
  public DigitalObjectJobPublisher(BranchHandler branchHandler, Component component, Job job) {
    this.branchHandler = branchHandler;
    this.logger = (ALogger)component;
    this.job = job;
    this.jobInfo = Jobs.getJobInfo(job);
  }
  
  public void processObjects() throws AspireException{
    AspireObject objects = job.get().get(ObjectsXml.OBJECTS_XML_TOP_LEVEL_ELEMENT);
    
    if (objects == null){
      return;
    }
    
    Map<String, Integer> sortNums = jobInfo.getSortNums();
    
    for (AspireObject object : objects.getChildren()) {
      if (Objects.isObject(object)) {
        AspireObject objectDoc = createJobData(object);
        String objectId = object.getAttribute("id");
        logger.debug("object %s-%s: %s",objectId,jobInfo.getNAID(),object.toXmlString());
        boolean sortNumChanged = false;
        if (sortNums.containsKey(objectId)) {
          int sortNum = sortNums.get(objectId);
          sortNumChanged = ((jobInfo.getPrevSortNums() != null && jobInfo.getPrevSortNums().get(objectId) != null && sortNum != jobInfo.getPrevSortNums().get(objectId)) ||
                  (jobInfo.getPrevSortNums() != null && jobInfo.getPrevSortNums().get(objectId) == null));

          setObjectSortNumber(objectDoc, sortNum);
        } else {
          logger.info("No sort num found for objectID: %s; naid: %s;",objectId,jobInfo.getNAID());
        }

        Job objectJob = createObjectJob(objectId, objectDoc);
        JobInfo objectJobInfo = setJobInfo(objectId, objectJob, sortNumChanged);

        process(objectJob);

        objectJobInfo.close();
      } 
    }
    
    populateContent();
  }
    
  private void process(Job objectJob) throws AspireException{
    JobInfo objectJobInfo = Jobs.getJobInfo(objectJob);
    String description = objectJobInfo.getDescription();
    
    try{
      logger.debug("Object processing: %s", description);
      branchHandler.process(objectJob, "onSubJob");
      logger.debug("Object completed: %s", description);
    } catch (Throwable ex){
      logger.error(ex, "Object failed: %s", description);
      objectJobInfo.getParent().getSubJobFailedCount().incrementAndGet();
    }
  }
  
  private AspireObject createJobData(AspireObject object) throws AspireException{
    // Start the digital object out the same as the description.
    AspireObject doc = job.get().clone();

    // Add objectId --- this is how everything downstream knows this is an object.
    String objectId = object.getAttribute("id");
    doc.add("objectId", objectId);
    AspireObject digitalObject = null;
    try {
      Map<String, AspireObject> digitalObjects = jobInfo.getDigitalObjects();
      if (digitalObjects.containsKey(objectId) ) {
        digitalObject = digitalObjects.get(objectId);
        String accessFileName = digitalObject.getText("accessFilename");
        doc.add("accessFilename", accessFileName.trim());
      } else {
        logger.info("Did not find object for objectId: %s in digitalObjects for %s",objectId,jobInfo.getNAID());
      }
    } catch(Exception e) {
      logger.error(e,"Error creating job data");
      if (digitalObject == null) {
        logger.error("Could not get Objects XML for objectID: %s; naid: %s",objectId,jobInfo.getNAID());
      } else {
        logger.error("Object XML for objectId: %s; naid: %s : %s",objectId,jobInfo.getNAID(),digitalObject.toXmlString(false));
      }
    }
    
    // Change the objects node to only have this object.
    // Create a new objects with only this one object in it.
    AspireObject strippedObjects = ObjectsXml.startNewObjects();
    strippedObjects.add(object.clone());
    doc.set(strippedObjects);

    // create new digitalObjectArray to remove all other objects but this one
    String descriptionType = "fileUnit";
    AspireObject description = doc.get("fileUnit");
    if (description == null) {
      descriptionType = "item";
      description = doc.get("item");
    }
    if (description == null) {
      descriptionType = "itemAv";
      description = doc.get("itemAv");
    }
    if (description == null) {
      descriptionType = "series";
      description = doc.get("series");
    }
    if (description == null) {
      descriptionType = "recordGroup";
      description = doc.get("recordGroup");
    }
    if (description == null) {
      descriptionType = "collection";
      description = doc.get("collection");
    }
    if (description != null) {
      AspireObject dasDigitalObjectArray = description.get("digitalObjectArray");
      if (dasDigitalObjectArray == null) {
        logger.debug("naid: %s - object ID: %s - DAS XML digitalObjectArray is null",jobInfo.getNAID(),objectId);
      } else if (dasDigitalObjectArray != null &&
              dasDigitalObjectArray.hasChildren()) {
        logger.debug("Looking for object ID: %s in digitalObjectArray",objectId);
        AspireObject strippedDigitalObjectArray = new AspireObject("digitalObjectArray");
        for (AspireObject child : dasDigitalObjectArray.getChildren()) {
          if ("digitalObject".equals(child.getName())) {
            AspireObject objectIdentifier = child.get("objectIdentifier");
            logger.debug("comparing to %s",objectIdentifier.getText());
            if (objectIdentifier.getText().equals(objectId)) {
              logger.debug("Found objectIdentifier matching %s in digitalObjectArray",objectId);
              strippedDigitalObjectArray.add(child);
              break;
            }
          }
        }
        try {
          description.set(strippedDigitalObjectArray);
          doc.set(description);
        }catch(Exception e){
          e.printStackTrace();
          logger.error("ERROR parring digitalObjectArray for naid: %s object ID: %s : %s",jobInfo.getNAID(),objectId,e.getMessage());
        }
      }
    }
    return doc;
  }

  private void setObjectSortNumber(AspireObject doc, int sortNumber) throws AspireException{
    doc.add("objectSortNumber", sortNumber);
  }
  
  private Job createObjectJob(String objectId, AspireObject doc) throws AspireException{
    String opaId = "obj-" + jobInfo.getNAID() + "-" + objectId;    
    String jobId = job.getJobId() + "/" + opaId;
    return JobFactory.newInstance(doc, jobId);
  }
  
  private JobInfo setJobInfo(String objectId, Job objectJob, boolean sortNumChanged) throws AspireException{
    JobInfo objectJobInfo = Jobs.addJobInfo(objectJob);
    objectJobInfo.setNAID(jobInfo.getNAID());
    objectJobInfo.setObjectsXmlLock(jobInfo.getObjectsXmlLock());    
    objectJobInfo.setOpaStorage(jobInfo.getOpaStorage());
    objectJobInfo.setFilesCopiedFromPreIngestion(jobInfo.getFilesCopiedFromPreIngestion());
    objectJobInfo.setParent(jobInfo);
    objectJobInfo.setObjectSortChanged(sortNumChanged);
    objectJobInfo.setExtractTextEnabled(jobInfo.isExtractTextEnabled());
    objectJobInfo.setForcedTextExtract(jobInfo.isForcedTextExtract());
    
    AspireObject object = objectJob.get()
            .get(ObjectsXml.OBJECTS_XML_TOP_LEVEL_ELEMENT)
            .get(ObjectsXml.OBJECTS_XML_OBJECT_ELEMENT);

    logger.debug("populating object for object ID: %s: %s",objectId,object.toXmlString());

    objectJobInfo.setDigitalObject(object);
    
    String description = String.format("object %s from %s", objectId, jobInfo.getDescription());
    objectJobInfo.setDescription(description);
    
    return objectJobInfo;
  }

  private void populateContent() throws AspireException {
    AspireObject digitalObjectsContent = job.get().get("digitalObjectsContent");
    
    for (AspireObject objectContent : jobInfo.getObjectContents()){
      digitalObjectsContent.add(objectContent);
    }
  }
}
