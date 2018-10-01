package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.JobEvent;
import com.searchtechnologies.aspire.services.JobEventHandler;
import com.searchtechnologies.aspire.services.logging.ALogger;

public class DigitalObjectJobEventHandler implements JobEventHandler {
  private final ALogger logger;
  private final JobEventHandler resumedJobEventHandler;
  
  public DigitalObjectJobEventHandler(Component component) throws AspireException{
    this.logger = (ALogger)component;
    this.resumedJobEventHandler = new ResumedJobEventHandler();
  }
    
  @Override
  public void processJobEvent(JobEvent jobEvent) throws AspireException {
    Job subJob = jobEvent.getJob();
    JobInfo subJobInfo = Jobs.getJobInfo(subJob);
    JobInfo parentJobInfo = subJobInfo.getParent();

    String objectDescription = String.format("%s from %s", subJobInfo.getObjectId(), parentJobInfo.getDescription());
    if (isUnhandledError(jobEvent)){
      logger.error("Object %s: %s %s", "failed", objectDescription, jobEvent.getResult().toXmlString(true));
      parentJobInfo.getSubJobFailedCount().incrementAndGet();
    } else{
      logger.info("Object %s: %s", "completed", objectDescription);
    }

    subJobInfo.close();
  }

  private boolean isUnhandledError(JobEvent jobEvent){
    return jobEvent.getEventType() == JobEvent.UNHANDLED_ERROR_EVENT;
  }
  
  private void resumeParentJob(JobInfo parentJobInfo) throws AspireException {
    logger.info("Resuming %s", parentJobInfo.getDescription());
    
    Job parentJob = parentJobInfo.getJob();
    
    if (parentJob.isOnBatch()){
      parentJob.setBatch(null);
    }
    
    parentJob.set(parentJobInfo.getJobData());
    Jobs.setJobInfo(parentJob, parentJobInfo);
    
    parentJob.registerListener(resumedJobEventHandler, JobEvent.JOB_COMPLETED_FLAGS);
    
    parentJobInfo.setJob(null);
    parentJobInfo.setJobData(null);
    
    parentJob.wake();
  }
  
  private class ResumedJobEventHandler implements JobEventHandler{

    @Override
    public void processJobEvent(JobEvent je) throws AspireException {
      Job job = je.getJob();
      JobInfo jobInfo = Jobs.getJobInfo(job);      
      
      String status = je.getEventType() == JobEvent.UNHANDLED_ERROR_EVENT ? "Failed" : "Completed";   
      logger.info("%s %s", status, jobInfo.getDescription());
      
      JobInfo parentJobInfo = Jobs.getJobInfo(jobInfo.getParentJob());
      parentJobInfo.getSubJobOutstandingCount().decrementAndGet();
      jobInfo.setParentJob(null);
    }
    
  }
}