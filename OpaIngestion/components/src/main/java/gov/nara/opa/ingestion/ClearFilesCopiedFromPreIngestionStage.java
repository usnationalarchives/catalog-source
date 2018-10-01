package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;
import java.io.File;

public class ClearFilesCopiedFromPreIngestionStage extends IngestionStage {
  
  @Override
  public void process(Job job) throws AspireException {
    JobInfo jobInfo = Jobs.getJobInfo(job);
    PreIngestionFileDeleter deleter = new PreIngestionFileDeleter(this);
    
    for (File file : jobInfo.getFilesCopiedFromPreIngestion()){
      deleter.deleteFile(file);      
    }
  }
}
