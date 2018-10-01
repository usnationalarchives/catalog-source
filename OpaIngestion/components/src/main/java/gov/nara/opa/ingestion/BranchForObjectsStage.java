/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.*;
import com.searchtechnologies.aspire.services.*;

/**
 * Branch processing based on whether digital objects are present.
 * @author OPA Ingestion Team
 */
public class BranchForObjectsStage extends StageImpl {

  public final String IS_OBJECT_EVENT = "isObject";

  /**
   * Branch processing based on whether digital objects are present.
   * @param j  The job to process.
   */
  @Override
  public void process(Job j) throws AspireException {
    AspireObject doc = j.get();

    if (doc.get("objectId") != null) {
      j.setBranch(IS_OBJECT_EVENT);
    }
  }


  /**
   * Release any resources that need to be released.
   */
  @Override
  public void close() {
    // bh.close();
  }


  /**
   * Initialize this component with the configuration data from the component manager
   * configuration. NOTE:  This method is *always* called, even if the component
   * manager configuration is empty (in this situation, "config" will be null).
   *
   * @param config The XML &lt;config&gt; DOM element which holds the custom configuration
   * for this component from the component manager configuration file.
   * @throws AspireException
   */
  @Override
  public void initialize(Element config) throws AspireException {
    // empty
  }
}
