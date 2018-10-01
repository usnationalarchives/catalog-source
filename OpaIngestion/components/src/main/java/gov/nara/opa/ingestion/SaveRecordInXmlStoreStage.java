package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;
import java.io.IOException;

/**
 * Stage to save record xml to a directory.
 */
public class SaveRecordInXmlStoreStage extends IngestionStage {
  
  @Override
  public void process(Job job) throws AspireException {
    JobInfo info = Jobs.getJobInfo(job);
    
    if (info.isProcessingAnnotations()){ 
      return;      
    }

    try {
      info.saveRecord();      
    } catch (IOException ex) {
      throw new AspireException("save record", ex);
    }
  }
}
