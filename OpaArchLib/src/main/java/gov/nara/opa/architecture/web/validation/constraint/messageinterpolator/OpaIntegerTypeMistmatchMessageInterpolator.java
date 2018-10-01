package gov.nara.opa.architecture.web.validation.constraint.messageinterpolator;

import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;

import org.springframework.validation.FieldError;

/**
 * @author aolaru
 * @date Jun 4, 2014 Provides the Opa default error code and message for fields
 *       whose values don't match the assigned regex pattern. It inserts into
 *       the message the name and the value of the validated field.
 */
public class OpaIntegerTypeMistmatchMessageInterpolator extends
    AbstractBaseMessageInterpolator {

  public static final int ERROR_CODE_ARGUMENTS_POSITION = 1;

  @Override
  protected String getErrorMessage(FieldError springError) {
    String errorMessage = String.format(
        ArchitectureErrorMessageConstants.INTEGER_TYPE_MISMATCH,
        springError.getField());
    return errorMessage;
  }

  @Override
  protected String getErrorCode(FieldError springError) {
    return ArchitectureErrorCodeConstants.INVALID_PARAMETER;
  }

}