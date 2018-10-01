package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Populating information in opa_titles table
 */
public class OpaTitlesTablePopulator {

  final Component component;
  final ALogger logger;
  final Job job;
  final JobInfo jobInfo;

  public OpaTitlesTablePopulator(Component component, Job job){
    this.component = component;
    this.logger = (ALogger)this.component;
    this.job = job;
    this.jobInfo = Jobs.getJobInfo(job);
  }

  /**
    * Puts record titles and other info in the opadb.
    * The information in the db is used for displaying information on
    * some My Contributions and Moderator pages, and to determine
    * whether a content detail page should show a viewer for objects.
    * @throws com.searchtechnologies.aspire.services.AspireException
    */
  public void populateOpaTilesTable(Connection connection) throws AspireException, SQLException {

      OpaDataBase opaDataBase = new OpaDataBase(component, job);

      if (connection != null){

          String naid = getNaid();
          String title = getTitle();
          String type = getType();
          String totalPages = getTotalPages();

          opaDataBase.upsertTitle(connection, naid, title, type, totalPages);
      }
  }

  private String getNaid()
  {
      String naid = "";

      if (jobInfo.getNAID() != null){
          naid = Integer.toString(jobInfo.getNAID());
      }

      return naid;
  }

  private String getTitle() throws AspireException
  {
      String title = "";

      if(jobInfo.isAuthorityRecord())
      {
          if (jobInfo.getRecord().get("termName") != null){
              title = jobInfo.getRecord().get("termName").getContent().toString();
          }
      }
      else
      {
        if (jobInfo.getRecord().get("title") != null){
            title = jobInfo.getRecord().get("title").getContent().toString();
        }
      }

      if (title.contains("'")) title = normalizeSingleQuote(title);

      return title;
  }

  private String getType() throws AspireException
  {
      String type = "";

      if(jobInfo.getRecordType() != null){
          type = jobInfo.getRecordType();
      }

      return type;
  }

  private String getTotalPages() throws AspireException
  {
      String totalPages = "0";

      if (jobInfo.getRecord().get("digitalObjectArray") != null){
          if(jobInfo.getRecord().get("digitalObjectArray").getAll("digitalObject") != null){
              totalPages = Integer.toString(jobInfo.getRecord().get("digitalObjectArray").getAll("digitalObject").size());
          }
      }

      return totalPages;
  }

  private String normalizeSingleQuote(String content)
  {
      return content.replace("'", "''");
  }


}
