package gov.nara.opa.api.validation.user.accounts;

import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

/**
 * Validator used for validating Administrator register
 * account requests
 * 
 */
@Component
public class RegisterAccountValidator extends OpaApiAbstractValidator {

	@Autowired
	private ConfigurationService configService;


	private static final LinkedHashSet<String> orderedValidatedItemCodes = new LinkedHashSet<String>();

	static {
		orderedValidatedItemCodes.add("userName");
		orderedValidatedItemCodes.add("password");
		orderedValidatedItemCodes.add("userType");
	}

	@Override
	protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
		return orderedValidatedItemCodes;
	}

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {

		validateFullNameForNaraStaff(validationResult, request);
		validateUserTypesAndRights(validationResult, request);
		//validateReferringUrl(validationResult, request);
	}

	private boolean validateFullNameForNaraStaff(ValidationResult validationResult,
			HttpServletRequest request) {
		RegisterAccountRequestParameters params = (RegisterAccountRequestParameters) validationResult.getValidatedRequest();

		//Validate full name is not empty when user is nara staff
		if(!StringUtils.isNullOrEmtpy(params.getEmail()) && 
				params.getEmail().toLowerCase().endsWith(configService.getConfig().getNaraEmail())) {
			//If user is a NARA staff account and displayFullName is not sent, set it as true for NARA accounts only.
			if(request.getParameter("displayFullName") == null) {
				params.setDisplayFullName(true);
			}
			if (StringUtils.isNullOrEmtpy(params.getFullName())) {
				ValidationError error = new ValidationError();
				error.setErrorCode(ArchitectureErrorCodeConstants.MISSING_PARAMETER);
				error.setErrorMessage(String.format(ArchitectureErrorMessageConstants.NOT_NULL_AND_NOT_EMPTY, "Full Name"));
				validationResult.addCustomValidationError(error);
				return false;
			}
			else if (!params.isDisplayFullName()) {
				ValidationError error = new ValidationError();
				error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
				error.setErrorMessage(String.format(ArchitectureErrorMessageConstants.INVALID_VALUE, "displayFullName", "true", "As a NARA Staff member you must display your full name"));
				validationResult.addCustomValidationError(error);
				return false;
			}
		}
		return true;
	}

	private boolean validateUserTypesAndRights(ValidationResult validationResult,
			HttpServletRequest request) {
		RegisterAccountRequestParameters params = (RegisterAccountRequestParameters)validationResult.getValidatedRequest();
		if (!params.getUserType().equals("standard")) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INSUFFICIENT_RIGHTS);
			error.setErrorMessage(String.format(ArchitectureErrorMessageConstants.INSUFFICIENT_RIGHTS, "register users", " to register this user type"));
			validationResult.addCustomValidationError(error);
			return false;
		}
		if (!params.getUserRights().equals("regular")) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INSUFFICIENT_RIGHTS);
			error.setErrorMessage(String.format(ArchitectureErrorMessageConstants.INSUFFICIENT_RIGHTS, "register users", " to register this user right"));
			validationResult.addCustomValidationError(error);
			return false;
		}
		return true;
	}
	
	private boolean validateReferringUrl(ValidationResult validationResult,
			HttpServletRequest request) {
		RegisterAccountRequestParameters params = (RegisterAccountRequestParameters)validationResult.getValidatedRequest();
		if(!StringUtils.isNullOrEmtpy(params.getReferringUrl())) {
			//Validate internal API only
			//request.getRequestURL()
			
			//Validate referringUrl is nara site
			String referringUrlPattern = configService.getConfig().getReferringUrlPattern();
			String referringUrl = params.getReferringUrl(); 
			if(!StringUtils.isNullOrEmtpy(referringUrl) && !referringUrl.matches(referringUrlPattern)) {
				ValidationUtils.setValidationError(validationResult, 
						ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE, 
						String.format(ArchitectureErrorMessageConstants.INVALID_VALUE, "referringUrl", "the current domain's url", ""), 
						"registerUser");
			}
			
		}
		
		return true;
	}

}
