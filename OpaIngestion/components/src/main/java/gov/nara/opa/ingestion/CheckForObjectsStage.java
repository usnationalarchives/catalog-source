/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.*;

/**
 * Stage to determine whether this description has objects.
 * @author OPA Ingestion Team
 */
public class CheckForObjectsStage extends IngestionStage {

  /**
   * Determine whether this description has objects.
   * If not, object processing can be skipped for it.
   * @param j  The job to process.
   */
  @Override
  public void process(Job j) throws AspireException {
    JobInfo info = Jobs.getJobInfo(j);
    
    // Determine whether this description has objects.
    boolean recordHasDigitalObjects = 
      (info.getRecord().get("digitalObjectArray") != null &&
       info.getRecord().get("digitalObjectArray").hasChildren());
    
    if (!recordHasDigitalObjects) {
      j.setBranch("noObjects");
    }
  }
}
