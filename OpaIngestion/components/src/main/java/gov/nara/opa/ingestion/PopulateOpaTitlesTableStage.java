/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;


import com.searchtechnologies.aspire.services.*;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Stage to populate information into opa_tiles table
 * @author OPA Ingestion Team
 */
public class PopulateOpaTitlesTableStage extends IngestionStage {

  private Settings settings;
  
  @Override
  public void initialize(Element config) throws AspireException {
    settings = Components.getSettings(this);
  }

  /**
   * Populating opa_tiles table with object information
     * @param job The job to process.
     * @throws com.searchtechnologies.aspire.services.AspireException
   */
  @Override
  public void process(Job job) throws AspireException {
    try (Connection connection = settings.getDbConnection()) {
      OpaTitlesTablePopulator populator = new OpaTitlesTablePopulator(this, job);
      populator.populateOpaTilesTable(connection);
    } catch (Throwable e) {
      JobInfo jobInfo = Jobs.getJobInfo(job);
      error(e, "%s", jobInfo.getDescription());
    }
  }
}
