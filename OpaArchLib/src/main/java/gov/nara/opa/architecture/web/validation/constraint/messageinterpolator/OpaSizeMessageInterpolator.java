package gov.nara.opa.architecture.web.validation.constraint.messageinterpolator;

import org.springframework.validation.FieldError;

/**
 * @author aolaru
 * @date Jun 4, 2014 Provides the Opa default error code and message for fields
 *       that do not meet the minimum and/or maxmimum size requirements. It
 *       inserts into the message the name of the validated field as well the
 *       limits for the size of the message.
 */
public class OpaSizeMessageInterpolator extends AbstractBaseMessageInterpolator {

  public static final int ERROR_CODE_ARGUMENTS_POSITION = 1;
  public static final int MAX_SIZE_ARGUMENTS_POSITION = 2;
  public static final int MIN_SIZE_ARGUMENTS_POSITION = 3;

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
        arguments[MIN_SIZE_ARGUMENTS_POSITION],
        arguments[MAX_SIZE_ARGUMENTS_POSITION]);
    return errorMessage;
  }

}
