package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.BranchHandler;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.util.concurrent.RejectedExecutionException;

public class JobPublisher {
  private final BranchHandler branchHandler;
  protected final ALogger logger;
    
  public JobPublisher(BranchHandler branchHandler, ALogger logger){
    this.branchHandler = branchHandler;
    this.logger = logger;
  }
  
  public boolean publish(Job job, String eventLabel){
    boolean published = false;
    
    while (!published){
      try {
        published = enqueue(job, eventLabel);
      } catch (AspireException ex) {
        logger.error(ex, "Unable to queue job %s", job.getJobId());
        break;
      }
    }
    
    return published;
  }
  
  private boolean enqueue(Job job, String eventLabel) throws AspireException {
    try {
      if (branchHandler.canEnqueueOrProcess(eventLabel)){
        branchHandler.enqueue(job, eventLabel);
        return true;
      }
    } catch (RejectedExecutionException ex) {  
      logger.info("Job %s has been rejected, waiting to retry", job.getJobId());
    }
    return false;
  }
  
  protected void execute(Job job, String eventLabel) throws AspireException{
      branchHandler.process(job, eventLabel);
  }
}
