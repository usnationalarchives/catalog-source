package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.exec.CommandLine;

public class ThumbnailCreator {
  static final Integer THUMBNAIL_MAX_WIDTH = 100;
  static final Integer THUMBNAIL_MAX_HEIGHT = 100;
    
  final Component component;
  final ALogger logger;
  final JobInfo jobInfo;
  final OpaStorage opaStorage;

  public ThumbnailCreator(Component component, Job job){
    this.component = component;
    this.logger = (ALogger)this.component;
    this.jobInfo = Jobs.getJobInfo(job);
    this.opaStorage = jobInfo.getOpaStorage();
  }
  
  public void createThumbnail() throws AspireException, IOException{
    if (jobInfo.isImage()){
      createImageThumbnail();
    } else if (jobInfo.isPDF()){
      createPDFThumbnail();
    }
  }
  
  public void createImageThumbnail() throws AspireException, IOException{
    
    String pathToImage = jobInfo.getPathToContent();
    String pathToThumbnail = jobInfo.getPathToThumbnail();

    if (opaStorage.isFileNewer(pathToImage, pathToThumbnail)){
      File image = jobInfo.getContentFile();
      
      File thumbnail = OpaFileUtils.getTempFile(UUID.randomUUID() + ".jpg");
              
      VIPS vips = new VIPS(component, image, thumbnail);
      vips.createThumbnails(THUMBNAIL_MAX_WIDTH, THUMBNAIL_MAX_HEIGHT);    
      
      opaStorage.saveFileAsPublic(thumbnail, pathToThumbnail);
      
      logger.info("Created thumbnail %s", pathToThumbnail);   
            
      thumbnail.delete();
    }
  }  

  /**
   * Creates a thumbnail image of the first page of the current digital object,
   *  if it is a PDF and if the resulting image is at least minSize
   * @throws com.searchtechnologies.aspire.services.AspireException
   */
  public void createPDFThumbnail() throws AspireException, IOException {
    
    String pathToPDF = jobInfo.getPathToContent();
    String pathToThumbnail = jobInfo.getPathToThumbnail();
    
    if (opaStorage.isFileNewer(pathToPDF, pathToThumbnail)){
      File pdf = jobInfo.getContentFile();
            
      File firstPageImage = OpaFileUtils.getTempFile(UUID.randomUUID() + ".jpg");
      
      // Expected command:
      // convert path-to-pdf[0] path-to-first-page.jpg
      CommandLine commandLine = new CommandLine("convert");
      commandLine.addArgument(pdf + "[0]");
      commandLine.addArgument(firstPageImage.toString());

      VIPS convert = new VIPS((Component)logger);
      convert.executeVipsCommand(commandLine);
      
      File thumbnail = OpaFileUtils.getTempFile(UUID.randomUUID() + ".jpg");
      
      VIPS vipsthumbnail = new VIPS((Component)logger, firstPageImage, thumbnail);
      vipsthumbnail.createThumbnails(THUMBNAIL_MAX_WIDTH, THUMBNAIL_MAX_HEIGHT);
      
      opaStorage.saveFileAsPublic(thumbnail, pathToThumbnail);
        
      logger.info("Created thumbnail %s", pathToThumbnail);   
      
      firstPageImage.delete();            
      thumbnail.delete();
    }
  } 
}
