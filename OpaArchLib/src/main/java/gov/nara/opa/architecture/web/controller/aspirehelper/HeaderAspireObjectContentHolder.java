package gov.nara.opa.architecture.web.controller.aspirehelper;

import java.util.LinkedHashMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.http.HttpStatus;

/**
 * Used for creating the header content in an API response
 * 
 * @author aolaru
 * @date Jun 9, 2014
 * 
 */
public class HeaderAspireObjectContentHolder extends AspireObjectContentHolder {

  public static final String HEADER_STATUS_PARAMETER_NAME = "@status";
  public static final String HEADER_TIME_PARAMETER_NAME = "time";
  public static final String HEADER_PATH_PARAMETER_NAME = "@path";
  public static final String HEADER_ACTION_PARAMETER_NAME = "@action";

  public static final String REQUEST_HEADER_NAME = "request";

  public HeaderAspireObjectContentHolder(HttpStatus httpStatus,
      String requestPath, String action) {
    this(httpStatus, requestPath, action, null);
  }

  public HeaderAspireObjectContentHolder(HttpStatus httpStatus,
      String requestPath, String action,
      LinkedHashMap<String, Object> requestParametersMap) {

    DateTime dt = new DateTime();
    String currentTimeStampString = dt.toDateTime(DateTimeZone.UTC).toString();

    aspireContent.put(HEADER_STATUS_PARAMETER_NAME, httpStatus.toString());
    aspireContent.put(HEADER_TIME_PARAMETER_NAME, currentTimeStampString);
    LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
    values.put(HEADER_ACTION_PARAMETER_NAME, action);
    values.put(HEADER_PATH_PARAMETER_NAME, requestPath);
    if (requestParametersMap != null) {
      values.putAll(requestParametersMap);
    }
    aspireContent.put(REQUEST_HEADER_NAME,
        new AspireObjectContentHolder(values));

  }
}
