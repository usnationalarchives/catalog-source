package gov.nara.opa.api.validation.search;

import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class StatisticsValidator extends OpaApiAbstractValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {

  }

}
