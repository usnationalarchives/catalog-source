package gov.nara.opa.api.validation.constraint.messageinterpolator;

import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.AbstractBaseMessageInterpolator;

import org.springframework.validation.FieldError;

/**
 * Provides the Opa default error code and message for
 * validation errors where an email cannot already exist in the Accounts
 * table emails. It inserts into the message the value of the validated
 * field.
 */

public class UserNameDoesNotExistAlreadyMessageInterpolator extends
    AbstractBaseMessageInterpolator {

  public static final int ERROR_CODE_ARGUMENTS_POSITION = 1;

  @Override
  protected String getErrorMessage(FieldError springError) {
    String errorMessage = springError.getDefaultMessage();
    errorMessage = String.format(errorMessage, springError.getRejectedValue());
    return errorMessage;
  }

  @Override
  protected String getErrorCode(FieldError springError) {
    Object[] arguments = springError.getArguments();
    return (String) arguments[ERROR_CODE_ARGUMENTS_POSITION];
  }

}