package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.Standards;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class InitializeDasUpdateStage extends IngestionStage {
  private static final String FEEDER_TYPE = "DasFeeder";
  
  @Override
  public void process(Job job) throws AspireException {
    AspireObject doc = job.get();
    doc.add(Standards.Basic.FEEDER_TYPE_TAG, FEEDER_TYPE);
    
    JobInfo jobInfo = Jobs.addJobInfo(job);
    
    AspireObject record = (AspireObject)doc.getContent("record");
      jobInfo.setRecord(record);
      job.get().set(record);

      doc.removeChildren("record");

      boolean forceFeed = (boolean)job.get().getContent("forceFeed");
      jobInfo.setForceFeed(forceFeed);
  }
  
}
