package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;


public class ProcessRemovedDigitalObjectsStage extends IngestionStage {
  
  @Override
  public void process(Job job) throws AspireException {
    JobInfo jobInfo = Jobs.getJobInfo(job);

    try {
        DigitalObjectsRemover remover = new DigitalObjectsRemover(this, job);
        remover.execute();
    } catch(Throwable ex){
      error(ex, "Process removed digital objects failed for %s", jobInfo.getDescription());
    }
  }  
}
