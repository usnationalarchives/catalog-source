package gov.nara.opa.architecture.web.validation;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;

/**
 * @author aolaru
 * @date Jun 4, 2014 It encapsulate the results of an Opa Validation.
 */
public class ValidationResult {
  private boolean isValid = true;
  private HttpStatus httpStatus;
  // list of validation errors collected as part of a particular controller
  // method validation - this includes all validation errors either
  // created by the AbstractBaseValidator or its subclasses.
  private List<ValidationError> errors = new ArrayList<ValidationError>();

  // Map of validatedItemCodes to their associated ValidationErrors. These
  // codes are either field names for field errors or custom codes to be
  // defined by the developers and associated with ValidationErrors. It will
  // be used by the AbstractBaseValidator to lookup up the default (the first)
  // validation error to be sent back to the caller.
  // Please if multiple validation errors are created for the same field only
  // one of these errors will be stored in this map for that particular field.
  // To get a list of all validation errors use the getErrors() accessor.
  //
  // IN TERMS OF FIELD NAMES: Spring and the AbstractBaseValidator use the
  // Java Bean naming convention to come up with the field names. For example
  // if the this property exists for this Request POJO:
  // RequestRegisterAccount.userName then the field name associated with
  // validations of this property will be 'userName'
  private ConcurrentHashMap<String, ValidationError> errorsByValidatedItemCode = new ConcurrentHashMap<String, ValidationError>();

  private String errorMessage;
  private String errorCode;

  private AbstractRequestParameters validatedRequest;

  // will hold any application specific object that are retrieved/constructed as
  // part of the validation and might be helpful for downstream processing
  private Map<String, Object> contextObjects;

  public boolean isValid() {
    return isValid;
  }

  public void setValid(boolean isValid) {
    this.isValid = isValid;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(HttpStatus httpStatus) {
    this.httpStatus = httpStatus;
  }

  public List<ValidationError> getErrors() {
    return errors;
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

  public AbstractRequestParameters getValidatedRequest() {
    return validatedRequest;
  }

  public void setValidatedRequest(AbstractRequestParameters validatedRequest) {
    this.validatedRequest = validatedRequest;
  }

  public ConcurrentHashMap<String, ValidationError> getErrorsByValidatedItemCode() {
    return errorsByValidatedItemCode;
  }

  public void addCustomValidationError(ValidationError error) {
    if (error == null) {
      throw new OpaRuntimeException("The error passed in can not be null");
    }
    setValid(false);
    errors.add(error);
    String validatedItemCode = error.getValidatedItemCode();
    if (validatedItemCode != null) {
      getErrorsByValidatedItemCode().put(validatedItemCode, error);
    }
    determineDefaultErrorMessage(null);
  }

  @Override
  public String toString() {
    return "Validation passed: " + isValid()
        + ". Validation result content: \n error code: " + getErrorCode()
        + "\n error message: " + getErrorMessage() + "\nhttp status: "
        + httpStatus + "\nerrors: " + getErrors();
  }

  public void addContextObject(String objectKey, Object object) {
    if (contextObjects == null) {
      contextObjects = new HashMap<String, Object>();
    }
    contextObjects.put(objectKey, object);
  }

  public Map<String, Object> getContextObjects() {
    if (contextObjects == null) {
      contextObjects = new HashMap<String, Object>();
    }
    return contextObjects;
  }

  /**
   * Determines which validation errors (if there are multiple) should be sent
   * back to the client
   * 
   * @param validationResult
   *          Once the default validation error is determine its message/code
   *          will be populated in the validationResult
   */
  public void determineDefaultErrorMessage(
      LinkedHashSet<String> orderedValidatedItemCodes) {
    if (isValid()) {
      return;
    }
    ValidationError defaultValidationError = getDefaultValidationError(orderedValidatedItemCodes);
    setErrorCode(defaultValidationError.getErrorCode());
    setErrorMessage(defaultValidationError.getErrorMessage());
  }

  /**
   * Determine the default validation error
   * 
   * @param validationResult
   *          The validationResult to be queried to find the default validation
   *          error
   * @return The default validation error
   */
  private ValidationError getDefaultValidationError(
      LinkedHashSet<String> orderedValidatedItemCodes) {
    if (isValid()) {
      return null;
    }
    Map<String, ValidationError> errorsByValidatedItemCode = getErrorsByValidatedItemCode();
    if (orderedValidatedItemCodes != null) {
      for (String validatedItemCode : orderedValidatedItemCodes) {
        ValidationError validationError = errorsByValidatedItemCode
            .get(validatedItemCode);
        if (validationError != null) {
          return validationError;
        }
      }
    }

    return getErrors().get(0);
  }
}
