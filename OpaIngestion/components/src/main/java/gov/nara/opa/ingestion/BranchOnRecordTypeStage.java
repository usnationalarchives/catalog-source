package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;

/**
 * Branches a job to either the Archival Description or the Authority Record
 * pipeline.
 */
public class BranchOnRecordTypeStage extends IngestionStage {
  
  @Override
  public void process(Job job) throws AspireException {
  
    JobInfo info = Jobs.getJobInfo(job);
    
    String branchEvent;
    
    if (info.isArchivalDescription()){
      branchEvent = "onArchivalDescription";
    } else if (info.isAuthorityRecord()){
      branchEvent = "onAuthorityRecord";
    } else{
      branchEvent = "onError";
    }

    job.setBranch(branchEvent);                    
  }
}
