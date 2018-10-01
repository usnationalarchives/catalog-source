package gov.nara.opa.architecture.web.validation;

import java.util.HashMap;

/**
 * @author aolaru
 * @date Jun 4, 2014 Opa Validation Error that encapsulates the main properties
 *       needed to process validation errors
 */
public class ValidationError {

  private String errorMessage;
  private String errorCode;
  private String validatedItemCode;
  private boolean isFieldValidationError = false;
  private HashMap<String, Object> contents;

  public String getValidatedItemCode() {
    return validatedItemCode;
  }

  public void setValidatedItemCode(String validatedItemCode) {
    this.validatedItemCode = validatedItemCode;
  }

  public boolean isFieldValidationError() {
    return isFieldValidationError;
  }

  public void setFieldValidationError(boolean isFieldValidationError) {
    this.isFieldValidationError = isFieldValidationError;
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

  public HashMap<String, Object> getContents() {
    return contents;
  }

  public void setContents(HashMap<String, Object> contents) {
    this.contents = contents;
  }

  @Override
  public String toString() {
    return "error code: " + getErrorCode() + "; error message: "
        + getErrorMessage() + "; isFieldError" + isFieldValidationError()
        + "; validatedItemCode: " + getValidatedItemCode();
  }

}
