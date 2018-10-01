package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;
import java.util.concurrent.atomic.AtomicInteger;
import org.w3c.dom.Element;

/**
 * Saves record hash to ingestion storage.
 */
public class SaveRecordHashStage extends IngestionStage{
  private IngestionDb ingestionDb;
  
  @Override
  public void initialize(Element config) throws AspireException {
    ingestionDb = Components.getIngestionDb(this);
  }

  @Override
  public void process(Job job) throws AspireException {
    JobInfo info = Jobs.getJobInfo(job);
    
    if (info.isProcessingAnnotations()){ 
      return;      
    }
    
    if (shouldSaveHash(info)){
      Integer naid = info.getNAID();
        String md5 = job.get().getText("md5");
      ingestionDb.setMD5(naid, md5);
    }
  }
  
  private boolean shouldSaveHash(JobInfo jobInfo){
    AtomicInteger subJobFailedCount = jobInfo.getSubJobFailedCount();
    return subJobFailedCount == null || subJobFailedCount.get() == 0;
  }
}
