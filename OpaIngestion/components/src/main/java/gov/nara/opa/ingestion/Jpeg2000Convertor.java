package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.io.File;
import java.io.IOException;
import org.apache.commons.exec.CommandLine;


public class Jpeg2000Convertor {
    
  final Component component;
  final ALogger logger;
  final Job job;
  final JobInfo jobInfo;

  public Jpeg2000Convertor(Component component, Job job){
    this.component = component;
    this.logger = (ALogger)this.component;
    this.job = job;
    this.jobInfo = Jobs.getJobInfo(job);
  }
  
  public void convertJpeg2000() throws AspireException, IOException{        
    
    OpaStorage opaStorage = jobInfo.getOpaStorage();
    
    String pathToJPEG2000Image = jobInfo.getPathToJPEG2000Image();
    
    String pathToJPEGImage = jobInfo.getPathToContent();
        
    if (opaStorage.isFileNewer(pathToJPEG2000Image, pathToJPEGImage)){
      
      File jpeg2000Image = opaStorage.getFile(pathToJPEG2000Image);
      
      File jpgImage = File.createTempFile("image", ".jpg");
              
      CommandLine commandLine = new CommandLine("convert");
      commandLine.addArgument(jpeg2000Image.toString());
      commandLine.addArgument(jpgImage.toString());

      VIPS vips = new VIPS(component);
      vips.executeVipsCommand(commandLine);
      
      opaStorage.saveFile(jpgImage, pathToJPEGImage);
      
      logger.info("Converted %s to %s", pathToJPEG2000Image, pathToJPEGImage); 
      
      jpgImage.delete();
    }
    
  }
}
