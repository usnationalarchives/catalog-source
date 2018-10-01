/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;

/**
 * Stage to send an update notifcation via the API when object page number changes.
 * @author OPA Ingestion Team
 */
public class SendPageUpdateNotificationStage extends IngestionStage {
  
  @Override
  public void process(Job job) throws AspireException {
    
    JobInfo jobInfo = Jobs.getJobInfo(job);
    AspireObject doc = job.get();

    if (jobInfo.getObjectSortChanged() && doc != null) {
      APIServer apiServer = new APIServer(this);
      apiServer.sendObjectUpdateNotification(jobInfo.getNAID(), jobInfo.getObjectId(), doc.getText("objectSortNumber"));      
    }
  }
}
