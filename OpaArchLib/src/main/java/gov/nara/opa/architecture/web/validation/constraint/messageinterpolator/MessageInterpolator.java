package gov.nara.opa.architecture.web.validation.constraint.messageinterpolator;

import gov.nara.opa.architecture.web.validation.ValidationError;

import org.springframework.validation.FieldError;

/**
 * @author aolaru
 * @date Jun 4, 2014 To be implemented by Opa MessageInterpolators
 */
public interface MessageInterpolator {
  /**
   * Interpolates the spring error message/code into an Opa specific error
   * message/code
   * 
   * @param springError
   *          Spring error which will be used to determine how the message/code
   *          are to be interpolated.
   * @param opaError
   *          Interpolated Opa validation error
   */
  void interpolate(FieldError springError, ValidationError opaError);
}
