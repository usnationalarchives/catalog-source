package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Populating information in opa_titles table
 */
public class OpaDataBase {
    
  final Component component;
  final ALogger logger;
  final Job job;
  final JobInfo jobInfo;

  public OpaDataBase(Component component, Job job){
    this.component = component;
    this.logger = (ALogger)this.component;
    this.job = job;
    this.jobInfo = Jobs.getJobInfo(job);
  }
  
  public void upsertTitle(Connection conn, String naid, String opaTitle, String opaType, String totalPages) throws SQLException {

    String sqlStatement = "INSERT INTO opa_titles VALUES ('" + naid + "','" + opaTitle + "','" + opaType + "'," + totalPages + ", 0)"
        + "ON DUPLICATE KEY UPDATE opa_title = '"+ opaTitle +"' , opa_type = '"+ opaType +"', total_pages = " + totalPages + ", deleted = 0" ;

    try (Statement statement = conn.createStatement()) {
      statement.executeUpdate(sqlStatement);
      statement.getResultSet();

      logger.debug("Opa title table successfully edited for naid %s.....", naid);
    }
  }

  public void deleteTitle(Connection conn, int naid) throws SQLException {
    String sqlStatement = "UPDATE opa_titles SET deleted = 1 WHERE na_id = '" + naid + "'";

    try (Statement statement = conn.createStatement()) {
      statement.executeUpdate(sqlStatement);
      logger.debug("Annotations DB Opa Title Table record successfully marked as deleted for naid %s.....", naid);
    }
  }
}
