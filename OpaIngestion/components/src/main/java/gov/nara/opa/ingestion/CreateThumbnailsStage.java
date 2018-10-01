/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.*;
import org.w3c.dom.Element;

/**
 * Stage to create thumbnail.
 * @author OPA Ingestion Team
 */
public class CreateThumbnailsStage extends IngestionStage {
  Settings settings;
  
  @Override
  public void initialize(Element config) throws AspireException {
    settings = Components.getSettings(this);
  }
  
  @Override
  public void process(Job job) throws AspireException {
    JobInfo jobInfo = Jobs.getJobInfo(job);

      if (jobInfo.isProcessingAnnotations()){
          return;
      }

    try{
      if (settings.createThumbnailsIsEnabled() 
            && (jobInfo.isImage() || jobInfo.isPDF())){
        ThumbnailCreator creator = new ThumbnailCreator(this, job);
        creator.createThumbnail();
      }   
    } catch(Throwable ex){
      jobInfo.getParent().getSubJobFailedCount().incrementAndGet();
      error(ex, "Thumbnail failed: %s", jobInfo.getDescription());
    }
  }
}
