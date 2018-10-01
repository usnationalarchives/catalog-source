package gov.nara.opa.api.response;

import gov.nara.opa.architecture.utils.TimestampUtils;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ResponseHelper {

  public static LinkedHashMap<String, Object> getHeaderItems(HttpStatus status,
      String requestPath, String action) {
    return getHeaderItems(status, requestPath, action, null);
  }

  public static LinkedHashMap<String, Object> getHeaderItems(HttpStatus status,
      String requestPath, String action,
      LinkedHashMap<String, Object> requestParams) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

    result.put("@status", String.valueOf(status));
    // TODO: Fix this
    result.put("time", TimestampUtils.getUtcTimestampString());

    // Put mandatory header parameters in map
    LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
    headerParams.put("@path", requestPath);
    headerParams.put("@action", action);

    // Add request params to header
    if (requestParams != null) {
      headerParams.putAll(requestParams);
    }

    result.put("request", headerParams);

    return result;
  }

  public static LinkedHashMap<String, Object> getErrorItems(HttpStatus status,
      String errorCode, String description,
      LinkedHashMap<String, Object> requestParams) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

    result.put("@code", errorCode);
    // TODO: Fix this
    result.put("description", description);

    if (requestParams != null) {
      result.putAll(requestParams);
    }

    return result;
  }

}
