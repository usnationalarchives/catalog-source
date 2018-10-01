package gov.nara.opa.api.validation.annotation.locks;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import gov.nara.opa.api.constants.AnnotationsConstants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

@Component
public class AnnotationLockValidator extends OpaApiAbstractValidator{

	/**
	 * Validates if the action has a valid value
	 * @param action
	 * 		action parameter sent
	 * @return 
	 * 		True if the validation process was successful, false otherwise
	 */
	protected boolean validateAction (String action, 
			ValidationResult validationResult) {
		if (!action.equals(AnnotationsConstants.LOCK_ACTION)  && 
				!action.equals(AnnotationsConstants.UNLOCK_ACTION)) {
			ValidationError validationError = new ValidationError();
			validationError.setErrorCode(
					ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
			validationError.setErrorMessage(
					ErrorConstants.INVALID_ACTION_VALUE);
			validationError.setFieldValidationError(true);
			validationError.setValidatedItemCode(AnnotationsConstants.ACTION_FIELD_NAME);
			validationResult.addCustomValidationError(validationError);
			return false;
		}
		return true;
	}

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		validateAction(request.getParameter(AnnotationsConstants.ACTION_FIELD_NAME), validationResult);
	}

}
