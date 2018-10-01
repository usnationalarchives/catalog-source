package gov.nara.opa.architecture.web.validation.constraint.messageinterpolator;

import org.springframework.validation.FieldError;

/**
 * @author aolaru
 * @date Jun 4, 2014 Provides the Opa default error code and message for fields
 *       whose values don't match the assigned regex pattern. It inserts into
 *       the message the name and the value of the validated field.
 */
public class OpaPatternMessageInterpolator extends
    AbstractBaseMessageInterpolator {

  public static final int ERROR_CODE_ARGUMENTS_POSITION = 1;
  public static final int PATTERN_ARGUMENTS_POSITION = 3;

  @Override
  protected String getErrorCode(FieldError springError) {
    Object[] arguments = springError.getArguments();
    return (String) arguments[ERROR_CODE_ARGUMENTS_POSITION];
  }

  @Override
  protected String getErrorMessage(FieldError springError) {
    Object[] arguments = springError.getArguments();
    String errorMessage = springError.getDefaultMessage();
    errorMessage = String.format(errorMessage, springError.getField(),
        arguments[PATTERN_ARGUMENTS_POSITION]);
    return errorMessage;
  }
}