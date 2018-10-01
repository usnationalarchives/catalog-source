package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.StageImpl;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.w3c.dom.Element;

public class SetFirstIngestDateStage extends StageImpl{
  private IngestionDb ingestionDb;
  private SimpleDateFormat solrDateFormatter;

  public static final String FIRST_INGEST_DATE_FIELD_NAME = "firstIngestDate";

  @Override
  public void initialize(Element elmnt) throws AspireException {
    ingestionDb = Components.getIngestionDb(this);
    solrDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    solrDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public void close() {
  }

  @Override
  public void process(Job job) throws AspireException {
    JobInfo info = Jobs.getJobInfo(job);
    Integer naId = info.getNAID();

      String firstIngestDate = ingestionDb.getFirstIngestDate(naId);

      if (firstIngestDate == null){
          firstIngestDate = solrDateFormatter.format(new Date());
          ingestionDb.setFirstIngestDate(naId, firstIngestDate);
      }

      // Put the first ingest date on the document
      job.get().set(FIRST_INGEST_DATE_FIELD_NAME, firstIngestDate);
  }
}
