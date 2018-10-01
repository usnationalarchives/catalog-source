package gov.nara.opa.api.validation.migration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

@Component
public class ListItemsMigrationValidator extends OpaApiAbstractValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    // TODO Auto-generated method stub

  }

}
