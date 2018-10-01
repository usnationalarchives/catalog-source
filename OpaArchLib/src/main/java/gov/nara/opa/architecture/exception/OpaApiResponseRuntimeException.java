package gov.nara.opa.architecture.exception;

import org.springframework.http.HttpStatus;

/**
 * @author aolaru
 * @date Jun 3, 2014 Common Exception to be used accross the app. Its initial
 *       main purpose is to wrap checked exception that we don't care
 *       propagating up the stack through method signatures and converting the
 *       exception to a runtime exception
 * 
 * 
 * 
 * ticket date       contributor                comments
 * 84300  2017-07-28 Matthew Mariano            changed extends from RuntimeException to OpaRuntimeException.
 *                                              note OpaRuntimeException extends RuntimeException.
 * 
 */
public class OpaApiResponseRuntimeException extends OpaRuntimeException {

  /**
   * used for serialization
   */
  private static final long serialVersionUID = 177743651L;

  private String errorMessage;
  private String errorCode;
  private HttpStatus httpStatus;
  private String action;
  

  public OpaApiResponseRuntimeException(Throwable exception) {
    super(exception);
  }

  public OpaApiResponseRuntimeException(String message) {
    super(message);
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(HttpStatus httpStatus) {
    this.httpStatus = httpStatus;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
  


}
