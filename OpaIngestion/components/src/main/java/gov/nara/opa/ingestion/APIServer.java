package gov.nara.opa.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.logging.ALogger;

public class APIServer {
  private static final String CHARSET = "UTF-8";
  
  private final ALogger logger;
  private final String notificationUrlFormat;
  
  public APIServer(Component component) throws AspireException{    
    this.logger = (ALogger)component;
    Settings settings = Components.getSettings(component);
    this.notificationUrlFormat = createNotificationUrlFormat(settings);
  }

  private String createNotificationUrlFormat(Settings settings) {
    return settings.getAPIServer() + "/OpaAPI/iapi/v1/ingestion/updatepagenumber/id/%s/objects/%s?pageNum=%s";
  }
  
  public void sendObjectDeleteNotification(Integer naid, String objectId) throws AspireException{
    logger.debug("Sending API Delete notification for digital object obj-%s-%s", naid, objectId);
    String notificationURL = String.format(notificationUrlFormat, naid, objectId, "-1");
    executeAPINotification(notificationURL);
  }
  
  public void sendObjectUpdateNotification(Integer naid, String objectId, String pageNumber) throws AspireException{
    logger.debug("Sending API Update notification for digital object obj-%s-%s: page number %s", naid, objectId, pageNumber);
    String notificationURL = String.format(notificationUrlFormat, naid, objectId, pageNumber);
    executeAPINotification(notificationURL);
  }

  private void executeAPINotification(String notificationURL) throws AspireException{
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(notificationURL).openConnection();
      connection.setRequestMethod("PUT");
      connection.setRequestProperty("Accept-Charset", CHARSET);            
      try (InputStream response = connection.getInputStream()) {
        logResponse(notificationURL, response);
      }
    } catch (IOException ex) {
      throw new AspireException("executeNotification", ex);
    }
  }
  
  private void logResponse(String requestUrl, InputStream response) throws IOException {
    if (logger.debugging()){
      logger.debug("Request: %s%nResponse: %s", requestUrl, IOUtils.toString(response, CHARSET));
    }
  }
}
