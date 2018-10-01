package gov.nara.opa.api.validation.system;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

@Component
public class LanguagesValidator extends OpaApiAbstractValidator {

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		validationResult.getValidatedRequest();
	}

}
