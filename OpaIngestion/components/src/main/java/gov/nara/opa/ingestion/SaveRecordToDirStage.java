package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import java.nio.file.Path;
import org.w3c.dom.Element;

/**
 * Stage to save record xml to a directory.
 */
public class SaveRecordToDirStage extends IngestionStage {
  public static final String DIR_TAG = "targetDir";
  private String targetDir;
  
  @Override
  public void initialize(Element config) throws AspireException {
    targetDir = getStringFromConfig(config, DIR_TAG, null);
  }
  
  @Override
  public void process(Job job) throws AspireException {
    JobInfo info = Jobs.getJobInfo(job);
    
    if (info.isProcessingAnnotations()){ 
      return;      
    }
    
    Integer naid = info.getNAID();
    
    if (naid == null){
      error("NAID not found for job %s", job.getJobId());
      return;
    }
    
    Path naidDir = NAIDDirectories.getSecondLevelDir(targetDir, naid);
    OpaFileUtils.createDirectories(naidDir);
    String fileName = String.format("%s.xml", naid);
    Path file = naidDir.resolve(fileName);    
    
    AspireObject record = info.getRecord();
    
    if (record == null){
      error("Record not found for job: %s", job.getJobId());
      return;
    }
    
    record.writeXml(file.toFile(), AspireObject.PRETTY);  
  }
}
