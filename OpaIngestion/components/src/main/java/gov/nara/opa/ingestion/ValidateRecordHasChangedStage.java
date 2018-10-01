package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;

/**
 * Checks to see if a hash exists for the current record, and compares hashes
 * to find out if the content is newer.
 * The job is terminated if the record content has not changed since last
 * ingestion.
 */
public class ValidateRecordHasChangedStage extends IngestionStage {
  private IngestionDb ingestionDb;
  private boolean reindexing;
  
  @Override
  public void initialize(org.w3c.dom.Element config) throws AspireException {
    ingestionDb = Components.getIngestionDb(this);
    reindexing = Components.getSettings(this).isReindex();
  }

  @Override
  public void process(Job job) throws AspireException {
	  
    JobInfo jobInfo = Jobs.getJobInfo(job);
    
    if (jobInfo.isProcessingAnnotations() ||
            jobInfo.isForceFeed()){
      return;      
    }
    
    Integer naid = jobInfo.getNAID();

      AspireObject doc = job.get();
      String md5 = doc.getText("md5");

      if (!reindexing && md5.equals(ingestionDb.getMD5(naid))){
          debug("%s has already been processed.", jobInfo.getDescription());
          job.terminate();
          return;
      }
  }
}
