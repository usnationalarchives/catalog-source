package gov.nara.opa.architecture.web.validation.constraint.messageinterpolator;

import gov.nara.opa.architecture.web.validation.ValidationError;

import org.springframework.validation.FieldError;

public abstract class AbstractBaseMessageInterpolator implements
    MessageInterpolator {

  @Override
  public void interpolate(FieldError springError, ValidationError opaError) {
    opaError.setErrorCode(getInternalErrorCode(springError));
    opaError.setErrorMessage(getErrorMessage(springError));
  }

  private String getInternalErrorCode(FieldError springError) {
    String errorCode = getErrorCode(springError);
    if (errorCode != null) {
      return errorCode;
    }
    Object[] springErrorArguments = springError.getArguments();
    if (springErrorArguments != null && springErrorArguments.length >= 2) {
      return (String) springErrorArguments[1];
    }
    return null;
  }

  /**
   * Subclasses need to provide the message to be used for the Opa Validation
   * error
   * 
   * @param springError
   * @return
   */
  protected abstract String getErrorMessage(FieldError springError);

  protected abstract String getErrorCode(FieldError springError);

}
