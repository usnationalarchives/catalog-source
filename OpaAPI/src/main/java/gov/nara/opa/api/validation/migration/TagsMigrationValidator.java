package gov.nara.opa.api.validation.migration;

import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class TagsMigrationValidator extends OpaApiAbstractValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    // TODO Auto-generated method stub

  }

}
