package gov.nara.opa.architecture.web.validation.constraint.messageinterpolator;

import org.springframework.validation.FieldError;

/**
 * @author aolaru
 * @date Jun 4, 2014 Provides the Opa default error code and message for
 *       password fields that do not meet the Opa password requirements. It
 *       inserts into the message the name of the validated field as well the
 *       limits for the size of the password.
 */

public class OpaUserPasswordMessageInterpolator extends
    AbstractBaseMessageInterpolator {
  public static final int ERROR_CODE_ARGUMENTS_POSITION = 1;
  public static final int MAX_SIZE_ARGUMENTS_POSITION = 2;
  public static final int MIN_SIZE_ARGUMENTS_POSITION = 3;

  @Override
  protected String getErrorCode(FieldError springError) {
    // Should be determined by the superclass
    return null;
  }

  @Override
  protected String getErrorMessage(FieldError springError) {
    Object[] arguments = springError.getArguments();
    String errorMessage = springError.getDefaultMessage();
    errorMessage = String.format(errorMessage,
        arguments[MIN_SIZE_ARGUMENTS_POSITION],
        arguments[MAX_SIZE_ARGUMENTS_POSITION]);
    return errorMessage;
  }
}