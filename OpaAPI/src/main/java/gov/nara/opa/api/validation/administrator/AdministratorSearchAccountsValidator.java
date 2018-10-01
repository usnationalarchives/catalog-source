package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountCollectionValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AdministratorSearchAccountsValidator extends
		CommonUserAccountValidator {

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {

		AdministratorSearchAccountsRequestParameters requestParameters = (AdministratorSearchAccountsRequestParameters) validationResult
				.getValidatedRequest();
		if (requestParameters.getStatus() != null
				&& requestParameters.getStatus().equals("0")) {
			requestParameters.setStatus(null);
		}

		if (!validateSort(validationResult, requestParameters)) {
			return;
		}

		List<UserAccountValueObject> users = getUserAccountDao().search(
				requestParameters);

		int totalCount = getUserAccountDao().getSearchTotalResults(
				requestParameters);

		// If using public API validate action=search
		if (!validateActionSearch(requestParameters, validationResult, request)) {
			return;
		}

		if (!this.validateUserAccountExists(
				(users == null || users.size() == 0) ? null : users.get(0),
				validationResult, requestParameters.getUserStatus(),
				ErrorConstants.NO_USERS_FOUND, requestParameters.getStatus())
				|| !validateAccountidSize(validationResult, request,
						requestParameters)) {
			return;
		}

		validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY,
				new UserAccountCollectionValueObject(users, totalCount));
	}

	protected boolean validateUserAccountExists(
			UserAccountValueObject userAccount,
			ValidationResult validationResult, Boolean status,
			String errorMessage, String userStatus) {
		if (userAccount == null
				|| (userStatus != null && (status != null && !userAccount
						.getAccountStatus().equals(status)))) {
			ValidationError validationError = new ValidationError();
			validationError.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
			if (errorMessage == null) {
				validationError
						.setErrorMessage(ErrorConstants.USER_NAME_DOES_NOT_EXIST);
			} else {
				validationError.setErrorMessage(errorMessage);
			}

			validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
			validationResult.addCustomValidationError(validationError);
			return false;
		}

		return true;
	}

	private boolean validateActionSearch(
			AdministratorSearchAccountsRequestParameters requestParameters,
			ValidationResult validationResult, HttpServletRequest request) {

		String requestPath = request.getServletPath();
		if (requestPath.contains("/" + Constants.PUBLIC_API_PATH + "/")
				&& (requestParameters.getAction() == null || requestParameters
						.getAction().isEmpty())) {
			ValidationError validationError = new ValidationError();
			validationError
					.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
			validationError
					.setErrorMessage(ErrorConstants.INVALID_ACTION_SEARCH_ONLY);

			validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
			validationResult.addCustomValidationError(validationError);

			return false;
		}

		return true;

	}

	private boolean validateAccountidSize(ValidationResult validationResult,
			HttpServletRequest request,
			AdministratorSearchAccountsRequestParameters requestParameters) {
		String userId = request.getParameter("internalId");
		if (userId != null && !userId.trim().equals("")
				&& requestParameters.getInternalId() == null) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
			error.setErrorMessage(ErrorConstants.USER_ID_TOO_LARGE);
			validationResult.addCustomValidationError(error);
			return false;
		}
		return true;
	}
}
