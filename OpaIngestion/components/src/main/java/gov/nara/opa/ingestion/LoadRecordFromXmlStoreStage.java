package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;

public class LoadRecordFromXmlStoreStage extends IngestionStage {

  @Override
  public void process(Job job) throws AspireException {
    JobInfo jobInfo = Jobs.getJobInfo(job);
    
    if (jobInfo.isProcessingAnnotations()){
      return;
    }

    try {
      jobInfo.setRecordFromXmlStore(jobInfo.getRecordInXmlStore());
    } catch (AspireException e){
      error(e, "Failed to load record from xml store for %s", jobInfo.getDescription());
    }
  }
}
