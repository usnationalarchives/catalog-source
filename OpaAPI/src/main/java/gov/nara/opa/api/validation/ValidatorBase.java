package gov.nara.opa.api.validation;

import gov.nara.opa.api.services.ErrorCode;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PathUtils;

/**
 * Base class for validators
 * 
 * @author lvargas
 *
 * @param <T>
 */
public class ValidatorBase<T extends ErrorCode> {

  protected T errorCode;
  protected Boolean isValid;
  protected String message;

  public T getErrorCode() {
    return errorCode;
  }

  public Boolean getIsValid() {
    return isValid;
  }

  public String getMessage() {
    return message;
  }

  public void resetValidation() {
    message = "";
    isValid = true;
  }

  public void setErrorCode(T errorCode) {
    this.errorCode = errorCode;
    this.message = errorCode.getErrorMessage();
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  public void setMessage(String message) {
    this.message = message;
    getErrorCode().setErrorMessage(message);
  }

  /**
   * Generic validator for required string values
   * 
   * @param valueName
   *          The name of the parameter
   * @param value
   *          The value of the parameter
   * @param errorMessage
   *          An optional error message
   * @return True if the string is not empty, false otherwise along with error
   *         settings
   */
  public boolean validateNotEmptyString(String valueName, String value,
      T errorCode) {
    if (value == null || value.isEmpty()) {
      isValid = false;
      message = ErrorConstants.missingParam;
      this.errorCode = errorCode;
      return false;
    }
    return true;
  }

  public void validateStringField(String fieldName, String value,
      int fieldSize, T errorCodeValue) {
    if (validateNotEmptyString(fieldName, value, errorCodeValue)) {
      if (value.length() > fieldSize) {
        isValid = false;
        errorCode = errorCodeValue;
        message = ValidationUtils.getFieldSizeExceededMessage(fieldName,
            ErrorConstants.fieldSizeExceeded, value.length(), fieldSize);
      }
    }
  }

  public void validateStringFieldValues(String fieldName, String value,
      String allowedValues, T errorCodeValue) {
    if (validateNotEmptyString(fieldName, value, errorCodeValue)) {
      if (!PathUtils.checkAllowedValues(value, allowedValues)) {
        isValid = false;
        this.errorCode = errorCodeValue;
        message = String.format(ErrorConstants.valueNotAllowed, fieldName);
      }
    }
  }

}
