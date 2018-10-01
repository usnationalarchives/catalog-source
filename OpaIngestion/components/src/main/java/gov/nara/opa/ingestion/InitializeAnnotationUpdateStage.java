/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;



import com.searchtechnologies.aspire.framework.AXPathFactory;
import com.searchtechnologies.aspire.framework.Standards;
import com.searchtechnologies.aspire.services.AXPath;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;

/**
 * Initializes the file and filename for the job, based on na_id and invalidates the hash record.
 * If the na_id is not found on the connector data, an error is logged and the job is terminated.
 *
 * @author OPA Ingestion Team
 */
public class InitializeAnnotationUpdateStage extends IngestionStage {
  private static final String FEEDER_TYPE = "AnnotationsFeeder";
  private static final AXPath connectorNAIDXPath = AXPathFactory.newInstance("doc/connectorSpecific/field[@name='na_id']");

  /** The main entry point for processing a job. This will be called, sometimes at the same
   * time by multiple threads (executing multiple jobs), whenever a job needs to be
   * processed by your component.
   * @param j  The job to process.
   */
  @Override
  public void process(Job j) throws AspireException {
    AspireObject doc = j.get();
    String naId = "";

    debug("Processing job: %s", j.getJobId());
    // Get NAID specified by connector.
    try {
      AspireObject naIdElement = connectorNAIDXPath.getElement(doc);
      if (naIdElement != null) {
        naId = naIdElement.getText();
      }
    } catch (AspireException ae) {
      error("InitializeAnnotationUpdateStage: No connector defined na_id field on job %s - %s", j.getJobId(), ae.getMsg());
      return;
    }

    debug("naId Found: %s", naId);

    Integer naidInt = Integer.parseInt(naId);    
    
    // Add feeder information to the document
    doc.setAttribute("action", "add");
    doc.add(Standards.Basic.FEEDER_TYPE_TAG, FEEDER_TYPE);

   // Add path information to the job
    JobInfo info = Jobs.addJobInfo(j);
    info.setNAID(naidInt);  
    info.setIsProcessingAnnotations(true);
  }

}
