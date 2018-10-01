package gov.nara.opa.api.validation.bulkImport;

import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class BulkImportValidator extends AbstractBaseValidator {

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
	}
}