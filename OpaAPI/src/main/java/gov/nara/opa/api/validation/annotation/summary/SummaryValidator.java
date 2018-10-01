package gov.nara.opa.api.validation.annotation.summary;

import gov.nara.opa.api.annotation.summary.SummaryErrorCode;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.NumericConstants;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class Created to manage the validation of parameters recieved to handle the
 * summary controller
 */
@Component
public class SummaryValidator extends OpaApiAbstractValidator {

	private final String VIEW_SUMMARY_ACTION = "viewContributionsSummary";

	@Autowired
	private ConfigurationService configService;

	private SummaryErrorCode errorCode;
	private Boolean isValid;
	private String message;

	/**
	 * Creates the message for database field size exceeded
	 * 
	 * @param fieldName
	 *            The name of the entity field
	 * @param currentLength
	 *            The content size
	 * @param limit
	 *            The database field size
	 * @return The formatted error message
	 */
	private String getFieldSizeExceededMessage(String fieldName,
			int currentLength, int limit) {
		return String
				.format("Text size of field '%1$s' (%2$d) is greater than field size: %3$d",
						fieldName, currentLength, limit);
	}

	public SummaryErrorCode getErrorCode() {
		return errorCode;
	}

	public Boolean getIsValid() {
		return isValid;
	}

	public String getMessage() {
		return message;
	}

	public void resetValidation() {
		message = "";
		isValid = true;
	}

	public void setErrorCode(SummaryErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	public void setMessage(String message) {
		this.message = message;
		getErrorCode().setErrorMessage(message);
	}

	/**
	 * Will set the response error code and description depending on the error
	 * cause.
	 * 
	 * @param errorCode
	 *            SummaryErrorCode that will depend on the error circunstances
	 */
	public void setStandardError(SummaryErrorCode errorCode) {
		setIsValid(false);
		setErrorCode(errorCode);
		switch (errorCode) {
		case MISSING_PARAM:
			getErrorCode().setErrorMessage("A parameter is missing");
			break;
		case TEXT_TO_LONG:
			getErrorCode().setErrorMessage("The text parameter is too long");
			break;
		case INVALID_VALUE:
			getErrorCode().setErrorMessage(
					"The value specified for a parameter is incorrect");
			break;
		case NOT_API_LOGGED_IN:
			getErrorCode().setErrorMessage(
					"You must be logged in to perform this action");
			break;
		case INSUFFICIENT_PRIVILEGES:
			getErrorCode()
					.setErrorMessage(
							"Moderator privileges are required to execute this operation");
			break;
		case CONTRIBUTIONS_NOT_FOUND:
			getErrorCode().setErrorMessage("No contributions found");
			break;
		case USER_NOT_FOUND:
			getErrorCode().setErrorMessage("Invalid username");
			break;
		default:
			break;
		}
	}

	/**
	 * Format parameter check (can equal: NULL, xml or json)
	 * 
	 * @param format
	 *            response format
	 * @return isValid (true/false)
	 */
	private boolean validateFormat(String format) {
		if (!format.equals("xml") && !format.equals("json")) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			setMessage("Format value is not valid");
		}
		return isValid;
	}

	/**
	 * Validate the naId parameter
	 * 
	 * @param naId
	 *            Parameter naId received on the request
	 * @return isValid (true/false)
	 */
	public boolean validateNaId(String naId) {
		if (naId == null || naId.isEmpty()) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			getErrorCode().setErrorMessage("Parameter naId cannot be empty");
		} else if (naId.length() > NumericConstants.NA_ID_LENGTH) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			setMessage(getMessage()
					+ (message.length() > 0 ? ". " : "")
					+ getFieldSizeExceededMessage("naId", naId.length(),
							NumericConstants.NA_ID_LENGTH));
		}
		return isValid;
	}

	/**
	 * Validate the objectId parameter
	 * 
	 * @param objectId
	 *            Parameter objectId received on the request
	 * @return isValid (true/false)
	 */
	public boolean validateObjectId(String objectId) {
		if (objectId == null || objectId.isEmpty()) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			getErrorCode()
					.setErrorMessage("Parameter objectId cannot be empty");
		} else if (objectId.length() > NumericConstants.OBJECT_ID_LENGTH) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			setMessage(getMessage()
					+ (message.length() > 0 ? ". " : "")
					+ getFieldSizeExceededMessage("objectId",
							objectId.length(),
							NumericConstants.OBJECT_ID_LENGTH));
		}
		return isValid;
	}

	/**
	 * Validate the include parameter
	 * 
	 * @param naId
	 *            Parameter include received on the request
	 * @return isValid (true/false)
	 */
	public boolean validateInclude(String include) {
		if (include == null || include.isEmpty()) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			getErrorCode().setErrorMessage("Parameter include cannot be empty");
		}
		return isValid;
	}

	/**
	 * Validate the parameters received on the request
	 * 
	 * @param parameters
	 *            Map with the parameters that we will validate.
	 */
	public void validateParameters(HashMap<String, Object> parameters) {

		resetValidation();

		if (parameters.containsKey("naId"))
			if (!validateNaId((String) parameters.get("naId")))
				return;

		if (parameters.containsKey("objectId"))
			if (!validateObjectId((String) parameters.get("objectId")))
				return;

		if (parameters.containsKey("include"))
			if (!validateInclude((String) parameters.get("include")))
				return;

		if (parameters.containsKey("format"))
			if (!validateFormat((String) parameters.get("format")))
				return;

		if (parameters.containsKey("pretty"))
			if (!validatePretty((boolean) parameters.get("pretty")))
				return;

		if (parameters.containsKey("offset"))
			if (!validateOffset((int) parameters.get("offset")))
				return;

		if (parameters.containsKey("rows"))
			if (!validateRows((int) parameters.get("rows")))
				return;

	}

	/**
	 * Pretty parameter check (can equal: true or false)
	 * 
	 * @param pretty
	 *            pretty-print (true/false)
	 * @return isValid (true/false)
	 */
	private boolean validatePretty(boolean pretty) {
		if (pretty != true && pretty != false) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			setMessage("Pretty value is not valid");
		}
		return isValid;
	}

	/**
	 * Pretty parameter check (can equal: true or false)
	 * 
	 * @param pretty
	 *            pretty-print (true/false)
	 * @return isValid (true/false)
	 */
	private boolean validateOffset(int offset) {
		if (offset < 0) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			setMessage("Invalid parameter 'offset' value");
		}
		return isValid;
	}

	/**
	 * Rows parameter check (can equal: true or false)
	 * 
	 * @param Rows
	 *            Rows parameter greater that zero but less than 200
	 * @return isValid (true/false)
	 */
	private boolean validateRows(int rows) {
		if (rows < 0) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			setMessage("Invalid parameter 'rows' value");
		}
		if (rows > configService.getConfig().getMaxSummaryRows()) {
			isValid = false;
			errorCode = SummaryErrorCode.INVALID_VALUE;
			setMessage(getMessage() + (message.length() > 0 ? ". " : "")
					+ " The maximum value for 'rows' is "
					+ configService.getConfig().getMaxSummaryRows());
		}
		return isValid;
	}

	private boolean validateRows(SummaryRequestParameters requestParameters,
			ValidationResult validationResult) {
		int maxRows = configService.getConfig().getMaxSummaryRows();
		if (requestParameters.getRows() + requestParameters.getOffset() > maxRows) {
			isValid = false;
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.INVALID_VALUE, String
							.format("The maximum value for 'rows' is %1$s",
									maxRows), VIEW_SUMMARY_ACTION);
		}

		return isValid;
	}

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {

		SummaryRequestParameters requestParameters = (SummaryRequestParameters) validationResult
				.getValidatedRequest();
		if (validationResult.isValid()) {
			if (!validateRows(requestParameters, validationResult)) {
			}
		}
	}

}
