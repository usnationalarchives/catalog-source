package gov.nara.opa.api.services;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Used to return results from the service layer to the controller
 */
public class ServiceResponseObject {
  private ErrorCode errorCode;
  private Object content;
  private HashMap<String, Object> contentMap;

  public ServiceResponseObject() {
    this.contentMap = new LinkedHashMap<String, Object>();
  }

  public ServiceResponseObject(ErrorCode errorCode, Object content) {
    this.errorCode = errorCode;
    this.content = content;
  }

  public ServiceResponseObject(ErrorCode errorCode,
      HashMap<String, Object> contentMap) {
    this.errorCode = errorCode;
    this.contentMap = contentMap;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public Object getContent() {
    return content;
  }

  public void setContent(Object content) {
    this.content = content;
  }

  public HashMap<String, Object> getContentMap() {
    return contentMap;
  }

  public void setContentMap(HashMap<String, Object> contentMap) {
    this.contentMap = contentMap;
  }

}
