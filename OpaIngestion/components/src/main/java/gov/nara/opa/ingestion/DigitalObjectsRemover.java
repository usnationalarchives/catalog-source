package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.utilities.StringUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DigitalObjectsRemover {
  private final ALogger logger;
  private final Job job;
  private final JobInfo jobInfo;
  private final SolrServer solrServer;
  private final APIServer apiServer;
  private final Component component;


  public DigitalObjectsRemover(Component component, Job job) throws AspireException{
    this.logger = (ALogger)component;
    this.job = job;
    this.jobInfo = Jobs.getJobInfo(this.job);
    this.solrServer = new SolrServer(component);
    this.apiServer = new APIServer(component);
    this.component = component;
  }


  public void execute() throws AspireException, IOException{
    deleteRemovedDigitalObjects();
    deleteObjectsXmlFileIfAllObjectsRemoved();    
  }

  private void deleteRemovedDigitalObjects() throws AspireException{
    deleteRemovedDigitalObjects(getRemovedDigitalObjects());
  }

  protected void deleteRemovedDigitalObjects(List<JobInfo> removedDigitalObjects) throws AspireException{
    if (removedDigitalObjects.isEmpty()){
      return;
    }

    logger.debug("Removing digital objects from job %s", job.getJobId());

    for (JobInfo digitalObject : removedDigitalObjects){
      //deleteDigitalObjectFiles(digitalObject);
      deleteIndexEntry(digitalObject);
      sendAPIObjectDeleteNotification(digitalObject);
    }

  }

  private List<JobInfo> getRemovedDigitalObjects() throws AspireException{
    List<JobInfo> removedDigitalObjects = new ArrayList<>();
    AspireObject recordFromXmlStore = jobInfo.getRecordFromXmlStore();

    if (recordFromXmlStore != null){
      AspireObject digitalObjectArray = recordFromXmlStore.get("digitalObjectArray");

      if (digitalObjectArray != null){
        for (AspireObject child : digitalObjectArray.getChildren()){
          if ("digitalObject".equals(child.getName())){
            try {
                boolean foundInCurrentRecord = existsInCurrentRecord(child);
                if (!foundInCurrentRecord) {
                    removedDigitalObjects.add(
                            new JobInfo(jobInfo, ObjectsXml.createObjectsXmlObject(component,recordFromXmlStore, child, null, null)));
                }
            } catch (Throwable e){
                logger.error("%s failed to create entry for object %s: %s",
                        jobInfo.getDescription(), child.toXmlString(true),
                        StringUtilities.exceptionTraceToString(e)
                );
            }
          }
        }
      }
    }
    return removedDigitalObjects;
  }

  private boolean existsInCurrentRecord(AspireObject digitalObject) throws AspireException {
    String objectId = digitalObject.getText("objectIdentifier");
    AspireObject digitalObjectArray = jobInfo.getRecord().get("digitalObjectArray");
    if (digitalObjectArray != null){
      for (AspireObject child : digitalObjectArray.getChildren()){
        if ("digitalObject".equals(child.getName())){
          if (objectId.equals(child.getText("objectIdentifier"))){
            return true;
          }
        }
      }
    }
    return false;
  }

  private void deleteDigitalObjectFiles(JobInfo jobInfo) throws AspireException {
    String relativePath = jobInfo.getPathToContent();
    if (relativePath.startsWith("/")) {
      relativePath = relativePath.substring(1);
    }
    if( relativePath.startsWith(jobInfo.getOpaStorage().LANDING_ZONE) ){
      // delete from new post-TO4 area
      jobInfo.getOpaStorage().deleteObjectAndOpaRenditions(relativePath);
    } else {
      // delete from legacy area
      logger.info("Deleting " + relativePath + " and artifacts from legacy storage area. NAID: " + jobInfo.getNAID());
      jobInfo.getOpaStorage().deleteLegacyObjects(relativePath);
    }
  }

  private void deleteIndexEntry(JobInfo jobInfo) throws AspireException {
    solrServer.deleteDigitalObject(jobInfo.getNAID(), jobInfo.getObjectId());
  }

  private void sendAPIObjectDeleteNotification(JobInfo jobInfo) throws AspireException {
    apiServer.sendObjectDeleteNotification(jobInfo.getNAID(), jobInfo.getObjectId());
  }
  
  private void deleteObjectsXmlFileIfAllObjectsRemoved() throws AspireException, IOException{
    if (recordHasDigitalObjects()){
      return;
    }

    jobInfo.getOpaStorage().deleteFiles(jobInfo.getPathToObjectsXml());
  }

  private boolean recordHasDigitalObjects() throws AspireException {
    AspireObject digitalObjectArray = jobInfo.getRecord().get("digitalObjectArray");
    if (digitalObjectArray != null){
      for (AspireObject child : digitalObjectArray.getChildren()){
          if ("digitalObject".equals(child.getName())){
            return true;
          }
      }
    }
    return false;
  }
}
