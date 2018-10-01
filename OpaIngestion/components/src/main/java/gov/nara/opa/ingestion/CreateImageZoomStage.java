/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;
import org.w3c.dom.Element;

public class CreateImageZoomStage extends IngestionStage {
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
      if (settings.createZoomImagesIsEnabled() && jobInfo.isImage()){
        TileCreator creator = new TileCreator(this, job);
        creator.CreateTiles();
      }
    } catch(Throwable ex){
      jobInfo.getParent().getSubJobFailedCount().incrementAndGet();
      error(ex, "Create deep zoom file failed for %s", jobInfo.getDescription());
    }
  }  
}