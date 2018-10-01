package gov.nara.opa.architecture.web.controller.aspirehelper;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;

/**
 * Used for creating a success API response by combining the header content with
 * content provided by an AbstractWebEntityValueObject
 * 
 * @author aolaru
 * @date Jun 9, 2014
 * 
 */
public class SuccessResponseAspireObjectContentHolder extends
    AspireObjectContentHolder {

  public static final String HEADER_HEADER_NAME = "header";

  /**
   * Constructor that initializes all parameters needed to construct the API
   * response. This overload is used on the success entity value object is
   * simple (i.e. it has no other entity value objects underneath it)
   * 
   * @param httpStatus
   *          HttpStatus - typically, HttpStatus.OK
   * @param requestPath
   *          The request path
   * @param action
   *          The action code
   * @param requestParametersMap
   *          The map containing all the parameters for the request
   * @param entityName
   *          The name to be used as the header of the success entity value
   *          object content
   * @param entityValueObject
   *          The value object that contains the data of the entity related with
   *          the request
   */
  public SuccessResponseAspireObjectContentHolder(HttpStatus httpStatus,
      String requestPath, String action,
      LinkedHashMap<String, Object> requestParametersMap, String entityName,
      AbstractWebEntityValueObject entityValueObject) {

    HeaderAspireObjectContentHolder headerObject = new HeaderAspireObjectContentHolder(
        httpStatus, requestPath, action, requestParametersMap);
    aspireContent.put(HEADER_HEADER_NAME, headerObject);
    if (entityName != null) {
      aspireContent.put(entityName, entityValueObject);
    }
  }

  public SuccessResponseAspireObjectContentHolder(HttpStatus httpStatus,
      String requestPath, String action,
      LinkedHashMap<String, Object> requestParametersMap,
      LinkedHashMap<String, Object> entityObjectsMap) {

    HeaderAspireObjectContentHolder headerObject = new HeaderAspireObjectContentHolder(
        httpStatus, requestPath, action, requestParametersMap);
    aspireContent.put(HEADER_HEADER_NAME, headerObject);
    if (entityObjectsMap != null) {
      aspireContent.putAll(entityObjectsMap);
    }
  }
}
