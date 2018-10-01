package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.StageImpl;
import com.searchtechnologies.aspire.services.AspireException;
import org.w3c.dom.Element;

/**
 * Base stage class for all Ingestion stages.
 */
public abstract class IngestionStage extends StageImpl {
  
  @Override
  public void initialize(Element config) throws AspireException {
  }

  @Override
  public void close() {
  }  
}
