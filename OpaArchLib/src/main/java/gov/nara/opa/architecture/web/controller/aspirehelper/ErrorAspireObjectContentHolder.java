package gov.nara.opa.architecture.web.controller.aspirehelper;

import java.util.LinkedHashMap;

/**
 * Used for creating the error content in an API response
 * 
 * @author aolaru
 * @date Jun 9, 2014
 * 
 */
public class ErrorAspireObjectContentHolder extends AspireObjectContentHolder {

  public static final String ERROR_CODE_PARAMETER_NAME = "@code";
  public static final String ERROR_MESSAGE_PARAMETER_NAME = "description";
  public static final String ERROR_CODE_REQ_PARAMS_HEADER_NAME = "error";

  public ErrorAspireObjectContentHolder(String errorCode,
      String errorDescription,
      LinkedHashMap<String, Object> requestParametersMap) {
    aspireContent.put(ERROR_CODE_PARAMETER_NAME, errorCode);
    aspireContent.put(ERROR_MESSAGE_PARAMETER_NAME, errorDescription);
    if (requestParametersMap != null) {
      aspireContent.putAll(requestParametersMap);
    }
  }

}
