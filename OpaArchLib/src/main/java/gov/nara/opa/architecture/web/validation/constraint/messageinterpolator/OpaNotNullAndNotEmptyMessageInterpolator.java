package gov.nara.opa.architecture.web.validation.constraint.messageinterpolator;

import org.springframework.validation.FieldError;

/**
 * @author aolaru
 * @date Jun 4, 2014 Provides the Opa default error code and message for fields
 *       that don't pass the is not null and is not empty validation. It inserts
 *       into the message the name of the validated field.
 */
public class OpaNotNullAndNotEmptyMessageInterpolator extends
    AbstractBaseMessageInterpolator {

  public static final int ERROR_CODE_ARGUMENTS_POSITION = 1;

  @Override
  protected String getErrorCode(FieldError springError) {
    Object[] arguments = springError.getArguments();
    return (String) arguments[ERROR_CODE_ARGUMENTS_POSITION];
  }

  @Override
  protected String getErrorMessage(FieldError springError) {
    String errorMessage = springError.getDefaultMessage();
    errorMessage = String.format(errorMessage, springError.getField());
    return errorMessage;
  }
}