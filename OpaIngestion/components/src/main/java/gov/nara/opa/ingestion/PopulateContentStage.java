/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;


import com.searchtechnologies.aspire.services.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Stage to populate IE/content field with extracted text.
 * @author OPA Ingestion Team
 */
public class PopulateContentStage extends IngestionStage {

  /**
   * Populate IE/content field with extracted text.
     * @param job The job to process.
     * @throws com.searchtechnologies.aspire.services.AspireException
   */
  @Override
  public void process(Job job) throws AspireException 
  {
    try{
      populateContent(job);
    } catch(Throwable ex){
      JobInfo jobInfo = Jobs.getJobInfo(job);
      jobInfo.getParent().getSubJobFailedCount().incrementAndGet();
      error(ex, "Populate content failed for %s", jobInfo.getDescription());
    }
  }
  
/**
   * This function read each digital object, create their zoom images and update the objects.xml file.
   * @param job 
   * @throws AspireException
   */
  public void populateContent(Job job) throws AspireException
  {    
    JobInfo info = Jobs.getJobInfo(job);
    if (!info.isPrimary()){
      return;
    }
    File extractedText = info.getExtractedTextFile();
    
    if (extractedText == null || !extractedText.exists()){
      debug("No content to populate in the aspire object for job: %s", job.getJobId());
      return;
    }          
    
    String content = readFileToString(extractedText);

    debug("Populating content in the aspire object for file in: %s", extractedText);

    AspireObject digitalObjectsContent = new AspireObject("digitalObjectsContent");

    String relativePathToTextFile = getRelativePathToTextFile(extractedText);
    AspireObject object = createObjectNode(info.getObjectId(), content, relativePathToTextFile);

    digitalObjectsContent.add(object);                  

    AspireObject doc = job.get();
    doc.add(digitalObjectsContent);

    Lock lock = info.getObjectsXmlLock();
    lock.lock();
    try{
      info.getParent().getObjectContents().add(object.clone());
    } finally{
      lock.unlock();
    }
    
    debug("Content successfully populated for file: %s", extractedText);
    
    if (OpaFileUtils.isTempFile(extractedText)){
      extractedText.delete();
    }
  }
  
  private String getRelativePathToTextFile(File file){
    String path = Paths.get(OpaStorageArea.OPA_RENDITIONS, OpaStorageArea.EXTRACTED_TEXT, file.getName()).toString();
    return FilenameUtils.separatorsToUnix(path);
  }
  
  public String readFileToString(File file) throws AspireException
  {
    try{
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }catch (IOException ex) {
      throw new AspireException("PopulateContent.extractText", ex);
    }
  }

  private AspireObject createObjectNode(String objectId, String content, String pathToTextFile){
    AspireObject objectNode = new AspireObject("object");
    objectNode.setAttribute("id", objectId);

    AspireObject contentNode = new AspireObject("content", content);
    contentNode.setAttribute("path", pathToTextFile);

    objectNode.add(contentNode);
    return objectNode;
  }

}
