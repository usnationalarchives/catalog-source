package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import java.io.File;

/**
 *
 * @author pmart�nez
 */
public abstract class CreateVIPImageStage extends IngestionStage{
    
  /**
   * This function gets the job from the process and create the zoom images.
   * @param job is the job in the pipeline
   * @throws AspireException 
   */
  @Override
  public void process(Job job) throws AspireException 
  {
    processDigitalObject(job);
  }
    
  protected abstract String getVIPImageType();
  
  /**
   * This function read each digital object, create their zoom images and update the objects.xml file.
   * @param digitalObject Are all digital objects
   * @param liveArea It is the root path for the currently element loaded.
   * @throws AspireException 
     * @throws java.io.IOException 
   */
  void processDigitalObject(Job job) throws AspireException
  {
    JobInfo info = Jobs.getJobInfo(job);
    
    AspireObject object = info.getDigitalObject();
      
    AspireObject fileNode = object.get("file");
    
    String type = fileNode.getAttribute("type");
    String mime = fileNode.getAttribute("mime");

    if (type.equals("primary") && mime.contains("image"))
    {
        String filename = fileNode.getAttribute("name");

        debug("Creating %s  for Image %s ....", getVIPImageType(), filename);

        OpaStorage opaStorage = info.getOpaStorage();
        createVipImage(filename, opaStorage);
    }
  }
   
   protected abstract File createVipImage(String filename, OpaStorage opaStorage) throws AspireException;
}
