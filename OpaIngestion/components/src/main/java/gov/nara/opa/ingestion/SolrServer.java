package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.apache.commons.io.IOUtils;

public class SolrServer {
  private static final String CHARSET = "UTF-8";
  
  private final ALogger logger;
  private final String updateUrlFormat;
  private final String getRecordUrlFormat;
  
  public SolrServer(Component component) throws AspireException{    
    this.logger = (ALogger)component;
    Settings settings = Components.getSettings(component);
    this.updateUrlFormat = createUpdateUrlFormat(settings);
    this.getRecordUrlFormat = createGetRecordUrlFormat(settings);
  }

  private String createUpdateUrlFormat(Settings settings) {
    return settings.getSolrServer() + "/solr/update?stream.body=%s&commitWithin=" + settings.getSolrCommitWithin();
  }

  private String createGetRecordUrlFormat(Settings settings) {
    return settings.getSolrServer() + "/solr/select?q=opaId:desc-%s&wt=json";
  }

  private String encode(String value) throws AspireException{
    try {
      return URLEncoder.encode(value, CHARSET);
    } catch (UnsupportedEncodingException ex) {
      throw new AspireException("URLEncoder.encode", ex);
    }
  }
  
  public void deleteDigitalObject(Integer naid, String objectId) throws AspireException{
    logger.debug("Deleting index entry of digital object obj-%s-%s", naid, objectId);
    String deleteQuery = encode(String.format("<delete><query>opaId:obj-%s-%s</query></delete>", naid, objectId));
    executeUpdateQuery(deleteQuery);
  }
  
  public void deleteRecord(Integer naid) throws AspireException{
    String deleteQuery = encode(String.format("<delete><query>naId:%s</query></delete>", naid));
    executeUpdateQuery(deleteQuery);
  }

  public String getRecord(Integer naid) throws AspireException {
    String url = createGetRecordUrl(naid.toString());
    logger.debug("SOLR GET RECORD URL: "+url);
    return executeGetRecord(url);
  }
  
  private String createUpdateUrl(String query){
    return String.format(updateUrlFormat, query);
  }

  private String createGetRecordUrl(String query) {
    return String.format(getRecordUrlFormat, query);
  }

  private void executeUpdateQuery(String query) throws AspireException{
    String updateUrl = createUpdateUrl(query);
    executeUpdate(updateUrl);
  }
  
  private void executeUpdate(String updateUrl) throws AspireException {
    try {
        URLConnection connection = new URL(updateUrl).openConnection();
        connection.setRequestProperty("Accept-Charset", CHARSET);            
        try (InputStream response = connection.getInputStream()) {
          logResponse(updateUrl, response);
        }
      } catch (IOException ex) {
        throw new AspireException("executeUpdate", ex);
      }
  }

  private String executeGetRecord(String getRecordUrl) throws AspireException {
    int tries = 3;
    while(tries > 0) {
      try {
        URLConnection connection = new URL(getRecordUrl).openConnection();
        connection.setRequestProperty("Accept-Charset", CHARSET);
        try (InputStream response = connection.getInputStream()) {
          String responseString = IOUtils.toString(response, CHARSET);
          return responseString;
        }
      } catch (IOException ex) {
        if (--tries == 0) {
          throw new AspireException("executeGetRecord", ex);
        }
      }
    }
    return null;
  }
  
  private void logResponse(String requestUrl, InputStream response) throws IOException {
    if (logger.debugging()){
      logger.debug("Request: %s%nResponse: %s", requestUrl, IOUtils.toString(response, CHARSET));
    }
  }

  private void logResponse(String requestUrl, String response) throws IOException {
    if (logger.debugging()){
      logger.debug("Request: %s%nResponse: %s", requestUrl, response);
    }
  }
}
