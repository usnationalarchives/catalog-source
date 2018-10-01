package gov.nara.opa.architecture.web.validation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindingResult;

/**
 * @author aolaru
 * @date Jun 4, 2014 Interface to be implemented by Opa Validators.
 */
public interface Validator {

  /**
   * Transforms the Spring validation errors into Opa validation errors and
   * performs any needed additional/custom validations.
   * 
   * @param bindingResult
   *          The Spring binding result to be validation. It will contain the
   *          bound request object and any validation errors collected by the
   *          Spring framework
   * @return Returns the result of the validation which will indicate if the
   *         validation is valid, what were the validation errors and the
   *         default error message/code to be used if only on Validation Error
   *         can be sent back to the client.
   */
  ValidationResult validate(BindingResult bindingResult,
      HttpServletRequest request);

  ValidationResult validate(BindingResult bindingResult,
      HttpServletRequest request, String apiType);

}
