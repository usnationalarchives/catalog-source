package gov.nara.opa.api.validation.contributionsStream;

import gov.nara.opa.api.moderator.contributionsStream.ContributionsStreamErrorCode;
import gov.nara.opa.api.moderator.contributionsStream.ContributionsStreamErrorConstants;
import gov.nara.opa.api.system.logging.APILogger;
import gov.nara.opa.api.validation.ValidatorBase;
import gov.nara.opa.common.NumericConstants;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

@Component
public class ContributionsStreamValidator extends
		ValidatorBase<ContributionsStreamErrorCode> {
	private static APILogger log = APILogger
			.getLogger(ContributionsStreamValidator.class);

	@Override
	public void resetValidation() {
		super.resetValidation();
		errorCode = ContributionsStreamErrorCode.NONE;
	}

	public void validate(LinkedHashMap<String, Object> parameters, boolean full) {
		String stringValue;

		// Offset
		validateOffset((int) parameters.get("offset"));
		if (!isValid)
			return;

		// Rows
		validateRows((int) parameters.get("rows"));
		if (!isValid)
			return;

		// NaId
		if (parameters.containsKey("naId")) {
			stringValue = (String) parameters.get("naId");
			if (stringValue != null && !stringValue.isEmpty()) {
				log.debug("validate", stringValue);
				validateNaId(stringValue);
			}
		}
		if (!isValid)
			return;

		// Filter type
		stringValue = (String) parameters.get("filterType");
		validateFilterType(stringValue);
		if (!isValid)
			return;

		// Format
		if (parameters.containsKey("format")) {
			stringValue = (String) parameters.get("format");
			validateStringFieldValues("format", stringValue, "json,xml",
					ContributionsStreamErrorCode.INVALID_PARAM);
		}
		if (!isValid)
			return;

	}

	public void validateOffset(int offset) {
		if (offset < 0) {
			isValid = false;
			message = String
					.format(ContributionsStreamErrorConstants.valueNotAllowed,
							"offset");
			errorCode = ContributionsStreamErrorCode.INVALID_PARAM;
		}
	}

	public void validateRows(int rows) {
		if (rows < 0) {
			isValid = false;
			message = String.format(
					ContributionsStreamErrorConstants.valueNotAllowed, "rows");
			errorCode = ContributionsStreamErrorCode.INVALID_PARAM;
		}
	}

	public void validateNaId(String naId) {
		validateStringField("NA ID", naId,
				NumericConstants.CONTRIBUTIONS_FILTER_LENGTH,
				ContributionsStreamErrorCode.INVALID_PARAM);
		errorCode.setErrorMessage(message);
	}

	public void validateFilterType(String filterType) {
		validateStringFieldValues("filter type", filterType,
				"TR,TG,CM,Moderator",
				ContributionsStreamErrorCode.INVALID_PARAM);
		errorCode.setErrorMessage(message);
	}

	public void setStandardError(ContributionsStreamErrorCode errorCode) {
		setIsValid(false);
		setErrorCode(errorCode);
		switch (errorCode) {
		case MISSING_PARAM:
			getErrorCode().setErrorMessage(
					ContributionsStreamErrorConstants.missingParam);
			break;
		case INVALID_PARAM:
			getErrorCode().setErrorMessage(
					ContributionsStreamErrorConstants.invalidParameter);
			break;
		case NOT_API_LOGGED_IN:
			getErrorCode().setErrorMessage(
					ContributionsStreamErrorConstants.notLoggedIn);
			break;
		case INSUFFICIENT_PRIVILEGES:
			getErrorCode().setErrorMessage(
					ContributionsStreamErrorConstants.moderatorRightsNeeded);
			break;
		default:
			break;
		}
	}

}
