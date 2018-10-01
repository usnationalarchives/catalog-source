package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;


public class LoadSortNumsStage extends IngestionStage {
  
  @Override
  public void process(Job job) throws AspireException {
    SortNumsLoader loader = new SortNumsLoader(this, job);
    loader.execute();
  }
  
}
