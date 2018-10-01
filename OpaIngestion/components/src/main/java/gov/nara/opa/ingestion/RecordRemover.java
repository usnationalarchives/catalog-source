package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.utilities.StringUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordRemover {
  private final ALogger logger;
  private final IngestionDb ingestionDb;
  private final SolrServer solrServer;
  private final Integer naid;
  private final JobInfo jobInfo;
  private final Job job;
  private final Component component;
  private final Connection connection;

  public RecordRemover(Component component, OpaStorageFactory factory, Integer naid, Connection connection, Job job) throws AspireException{
    this.logger = (ALogger)component;  
    this.ingestionDb = Components.getIngestionDb(component);
    this.solrServer = new SolrServer(component);
    this.naid = naid;
    this.job = job;
    this.jobInfo = Jobs.getJobInfo(job);
    this.component = component;
    this.connection = connection;
    jobInfo.setNAID(naid);
    jobInfo.setOpaStorage(factory.createOpaStorage(naid));
  }
  
  public void execute() throws AspireException {
    deleteOpaIP();
    deleteFromIngestionDb();
    deleteFromSearchEngine();
    updateOpaTitlesTable();
    logger.info("Deleted record '%s'", naid);
  }

  private void deleteOpaIP() throws AspireException {
    List<JobInfo> removedDigitalObjects = new ArrayList<>();
    AspireObject recordFromXmlStore = jobInfo.getRecordInXmlStore(naid);

    if (recordFromXmlStore != null){
      AspireObject digitalObjectArray = recordFromXmlStore.get("digitalObjectArray");

      if (digitalObjectArray != null){
        for (AspireObject child : digitalObjectArray.getChildren()){
          if ("digitalObject".equals(child.getName())){
            try {
              removedDigitalObjects.add(
                      new JobInfo(jobInfo, ObjectsXml.createObjectsXmlObject(component, recordFromXmlStore, child, null, null)));
            } catch (Throwable e){
              logger.error("%s failed to create entry for object %s: %s",
                      jobInfo.getDescription(), child.toXmlString(true),
                      StringUtilities.exceptionTraceToString(e)
              );
            }
          }
        }
      }
    }

    logger.info("Deleting digital objects for NAID: "+naid);
    try {
      DigitalObjectsRemover remover = new DigitalObjectsRemover(component, job);
      remover.deleteRemovedDigitalObjects(removedDigitalObjects);
    } catch (Throwable e) {
      logger.error("Failed to delete digital objects for NAID: "+naid+"\n"+StringUtilities.exceptionTraceToString(e));
    }
    logger.info("Successfully deleted digital objects for NAID: "+naid);
  }
  
  private void deleteFromSearchEngine() throws AspireException {
    logger.info("Deleting naid %s from Solr", naid);
    solrServer.deleteRecord(naid);
  }

  private void deleteFromIngestionDb() {   
    logger.info("Deleting record %s from Ingestion DB", naid);
    ingestionDb.removeRecord(naid);
  }

  private void updateOpaTitlesTable(){
    logger.info("Marking record %s as deleted in Annotations DB Opa Titles table", naid);
    OpaDataBase opaDataBase = new OpaDataBase(component, job);
    if (connection != null){
      try {
        opaDataBase.deleteTitle(connection, naid);
        connection.close();
      } catch (SQLException e) {
        logger.error("Error marking record as deleted in Annotations DB Opa Titles table for naid %s", naid);
      }
    }
  }

  private void deleteFromOldLocation(){
    logger.info("Deleting objects from old location for naid %s",naid);

  }

}
