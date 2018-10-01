package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import java.nio.file.Files;
import java.nio.file.Path;
import org.w3c.dom.Element;

/**
 * Represents the Quarantine area in Xml Store.
 */
public class Quarantine extends ComponentImpl {
  static final String QUARANTINE_DIR_TAG = "quarantineDir";
  String quarantineDir;
  
  @Override
  public void initialize(Element config) throws AspireException {
    quarantineDir = getStringFromConfig(config, QUARANTINE_DIR_TAG, null);
  }

  @Override
  public void close() {
    
  }
  
  public void quarantine(Job job) throws AspireException{
    JobInfo info = Jobs.getJobInfo(job);
    
    Integer naid = info.getNAID();
    
    if (naid == null){
      error("NAID not found for job %s", job.getJobId());
      return;
    }
    
    AspireObject record = info.getRecord();
    
    if (record == null){
      error("Record not found for job: %s", job.getJobId());
      return;
    }
    
    Path naidDir = NAIDDirectories.getSecondLevelDir(quarantineDir, naid);
    OpaFileUtils.createDirectories(naidDir);

    // Save record
    String recordFileName = String.format("%s.xml", naid);
    Path file = naidDir.resolve(recordFileName);            
    record.writeXml(file.toFile(), AspireObject.PRETTY);  

    // Save job result
    Path resultFile = getResultFile(naidDir, naid.toString());   
    job.getResultForUse().writeXml(resultFile.toFile(), AspireObject.PRETTY);  
  }
    
  /**
   * Builds the path to a file that will contain job results.
   * The file name has the pattern {record-file-name}-result.xml.
   * If the file already exists, an integer is added to the end of the file
   * name to guarantee that the returned path does not exist and can be used
   * to save job results.
   * @param baseDir
   * @param baseName
   * @return 
   */
  Path getResultFile(Path baseDir, String baseName){
    Path resultFile = baseDir.resolve(baseName + "-result.xml");
    
    int index = 0;
    while (Files.exists(resultFile)){
      index++;
      resultFile = baseDir.resolve(String.format("%s-result-%d.xml", baseName, index));
    }
    
    return resultFile;
  }
}
