/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;


import com.searchtechnologies.aspire.services.*;
import org.w3c.dom.Element;

/**
 * Stage to convert Jpeg2000 images to an alternative representation.
 * @author OPA Ingestion Team
 */
public class ConvertJpeg2000Stage extends IngestionStage {
    
  Settings settings;
  
  @Override
  public void initialize(Element config) throws AspireException {
    settings = Components.getSettings(this);
  }

  /**
   * Convert Jpeg2000 images to an alternative representation.
     * @param job The job to process.
     * @throws com.searchtechnologies.aspire.services.AspireException
   */
  @Override
  public void process(Job job) throws AspireException {
    JobInfo jobInfo = Jobs.getJobInfo(job);

      if (jobInfo.isProcessingAnnotations()){
          return;
      }

    try{
      if (settings.convertJpeg2000IsEnabled() && jobInfo.isJPEG2000Image()){
          Jpeg2000Convertor convertor = new Jpeg2000Convertor(this, job);
          convertor.convertJpeg2000();
      }
    } catch(Throwable ex){  
      jobInfo.getParent().getSubJobFailedCount().incrementAndGet();
      error(ex, "Convert jpeg2000 failed for %s", jobInfo.getDescription());
    }
  }
}
