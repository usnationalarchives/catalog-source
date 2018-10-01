package gov.nara.opa.architecture.web.controller.aspirehelper;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;

/**
 * Creates an Error Reponse by combining the header content with the error
 * content
 * 
 * @author aolaru
 * @date Jun 9, 2014
 * 
 */
public class ErrorResponseObjectContentHolder extends AspireObjectContentHolder {

  public static final String HEADER_HEADER_NAME = "header";
  public static final String ERROR_HEADER_NAME = "error";

  public ErrorResponseObjectContentHolder(HttpStatus httpStatus,
      String requestPath, String action, String errorCode,
      String errorDescription,
      LinkedHashMap<String, Object> requestParametersMap) {

    aspireContent.put(HEADER_HEADER_NAME, new HeaderAspireObjectContentHolder(
        httpStatus, requestPath, action));
    aspireContent.put(ERROR_HEADER_NAME, new ErrorAspireObjectContentHolder(
        errorCode, errorDescription, requestParametersMap));

  }

}
