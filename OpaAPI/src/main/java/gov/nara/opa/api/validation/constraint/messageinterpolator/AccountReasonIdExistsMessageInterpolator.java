package gov.nara.opa.api.validation.constraint.messageinterpolator;

import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.AbstractBaseMessageInterpolator;

import org.springframework.validation.FieldError;

public class AccountReasonIdExistsMessageInterpolator extends
    AbstractBaseMessageInterpolator {

  public static final int ERROR_CODE_ARGUMENTS_POSITION = 1;

  @Override
  protected String getErrorMessage(FieldError springError) {
    String errorMessage = springError.getDefaultMessage();
    return errorMessage;
  }

  @Override
  protected String getErrorCode(FieldError springError) {
    Object[] arguments = springError.getArguments();
    return (String) arguments[ERROR_CODE_ARGUMENTS_POSITION];
  }

}