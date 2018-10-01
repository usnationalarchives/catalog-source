package gov.nara.opa.api.validation.annotation.transcriptions;

import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorConstants;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.AnnotationsCommonValidator;
import gov.nara.opa.common.NumericConstants;
import gov.nara.opa.common.storage.OpaStorageFactory;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Parameter validation for Transcriptions
 */
@Component
public class TranscriptionValidator extends AnnotationsCommonValidator {

	@Autowired
	private PageNumberUtils pageNumberUtils;

	@Autowired
	private OpaStorageFactory opaStorageFactory;

	private Boolean isValid;
	private TranscriptionErrorCode errorCode;
	private String message;

	/**
	 * @return True if the validation process was successful, false otherwise
	 */
	public Boolean getIsValid() {
		return isValid;
	}

	/**
	 * Forces the validity value
	 * 
	 * @param isValid
	 *            The validity value to set
	 */
	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	public TranscriptionErrorCode getErrorCode() {
		if (!isValid) {
			if (errorCode == TranscriptionErrorCode.NONE) {
				errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
			}
			errorCode.setErrorMessage(message);
		} else {
			errorCode = TranscriptionErrorCode.NONE;
		}
		return errorCode;
	}

	public void setErrorCode(TranscriptionErrorCode errorCode) {
		this.errorCode = errorCode;
		this.message = errorCode.getErrorMessage();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Clears the current validation values and sets the status to no error
	 */
	public void resetValidation() {
		errorCode = TranscriptionErrorCode.NONE;
		message = "";
		isValid = true;
	}

	/**
	 * Validates a set of parameters
	 * 
	 * @param parameters
	 *            A hashmap with the name and value of the request parameters
	 * @param full
	 *            If true, a full field validation will be performed regardless
	 *            of the fields contained in the parameter hashmap. If false it
	 *            limits to validate only the parameters in the hashmap.
	 */
	public void validate(LinkedHashMap<String, Object> parameters, boolean full) {
		validate(parameters, full, false);
	}

	/**
	 * Validates a set of parameters
	 * 
	 * @param parameters
	 *            A hashmap with the name and value of the request parameters
	 * @param full
	 *            If true, a full field validation will be performed regardless
	 *            of the fields contained in the parameter hashmap. If false it
	 *            limits to validate only the parameters in the hashmap.
	 * @param isAdding
	 *            If true it runs the validation for when a transcription is
	 *            added
	 */
	public void validate(LinkedHashMap<String, Object> parameters,
			boolean full, boolean isAdding) {
		validateParameters(parameters, full, isAdding);
	}

	/**
	 * Validates the provided parameters that represent a user account
	 * 
	 * @param parameters
	 *            A hashmap with the name and value of the request parameters
	 * @param full
	 *            If true, a full field validation will be performed regardless
	 *            of the fields contained in the parameter hashmap. If false it
	 *            limits to validate only the parameters in the hashmap.
	 * @param isAdding
	 *            If true it runs the validation for when a transcription is
	 *            added
	 */
	private void validateParameters(LinkedHashMap<String, Object> parameters,
			boolean full, boolean isAdding) {
		String stringValue;

		resetValidation();

		// Api Type
		stringValue = (String) parameters.get("apiType");
		validateApiType(stringValue);
		if (!isValid)
			return;

		// NA ID
		if (full || (parameters.containsKey("naId") && isAdding)) {
			stringValue = (String) parameters.get("naId");
			validateNaIds(stringValue);
		}
		if (!isValid)
			return;

		// Object Id
		if (full || (parameters.containsKey("objectId") && isAdding)) {
			stringValue = (String) parameters.get("objectId");
			String naId = (String) parameters.get("naId");
			validateObjectIds(stringValue, naId);
		}
		if (!isValid)
			return;

		// Action
		if (parameters.containsKey("action")) {
			stringValue = (String) parameters.get("action");
			validateStringFieldValues("action", stringValue,
					"lock,unlock,saveAndRelock,saveAndUnlock");
		}
		if (!isValid)
			return;

		// Format
		if (parameters.containsKey("format")) {
			stringValue = (String) parameters.get("format");
			validateStringFieldValues("format", stringValue, "json,xml");
		}
		if (!isValid)
			return;

		// Reason Id
		if (parameters.containsKey("reasonId")) {
			int reasonId = (int) parameters.get("reasonId");
			validateReasonId(reasonId);
		}
		if (!isValid)
			return;

		// Notes
		if (parameters.containsKey("notes")) {
			stringValue = (String) parameters.get("notes");
			validateNotes(stringValue);
		}
		if (!isValid)
			return;

		// Version number
		if (parameters.containsKey("versionNumber")) {
			int versionNumber = (int) parameters.get("versionNumber");
			validateVersionNumber(versionNumber);
		}

		// Version string
		if (parameters.containsKey("version")) {
			stringValue = (String) parameters.get("version");
			validateStringVersionNumber(stringValue);
		}

	}

	private void validateStringVersionNumber(String versionNumber) {
		if (!versionNumber.equals("all") && !versionNumber.matches("[0-9]+")) {
			isValid = false;
			message = TranscriptionErrorConstants.invalidVersionNumber;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
		}

	}

	private void validateVersionNumber(int versionNumber) {
		if (versionNumber < 0) {
			isValid = false;
			message = TranscriptionErrorConstants.invalidVersionNumber;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
		}
	}

	public void validateReasonId(int reasonId) {
		if (reasonId < 0) {
			isValid = false;
			message = TranscriptionErrorConstants.invalidReasonId;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
		}
	}

	public void validateAccountId(int accountId) {
		if (accountId <= 0) {
			isValid = false;
			message = TranscriptionErrorConstants.invalidAccountId;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
		}
	}

	public void validateNotes(String notes) {
		if (notes.length() > NumericConstants.NOTES_LENGTH) {
			isValid = false;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
			message = String.format(
					TranscriptionErrorConstants.fieldSizeExceeded, "Notes",
					notes.length(), NumericConstants.NOTES_LENGTH);
		}
	}

	public void validateApiType(String apiType) {
		validateStringFieldValues("Api Type", apiType,
				Constants.PUBLIC_API_PATH + "," + Constants.INTERNAL_API_PATH);
	}

	public void validateNaIds(String naId) {
		if (!validateNaId(naId)) {
			isValid = false;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
			message = String.format(TranscriptionErrorConstants.INVALID_NA_ID);
		}
	}

	public void validateObjectIds(String objectId, String naId) {
		validateStringFields("Object ID", objectId,
				NumericConstants.OBJECT_ID_LENGTH);

		if (!validateObjectId(objectId, naId)) {
			isValid = false;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
			message = String
					.format(TranscriptionErrorConstants.INVALID_OBJECT_ID);
		}
	}

	public void validateLanguageISO(String languageISO) {
		if (languageISO.length() != NumericConstants.LANGUAGE_ISO_LENGTH) {
			isValid = false;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
			message = String.format(
					TranscriptionErrorConstants.invalidFieldSize,
					"Language ISO", NumericConstants.LANGUAGE_ISO_LENGTH);
		}
	}

	public void validateUnexpectedParameters(HttpServletRequest request,
			String validParameters) {
		String additionalParams = PathUtils.checkAdditionalParameters(request,
				validParameters);
		if (additionalParams != null && !additionalParams.isEmpty()) {
			// send error message
			isValid = false;
			errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
			message = String.format(
					TranscriptionErrorConstants.unexpectedParameters,
					additionalParams);

		}
	}

	public void validateStringFieldValues(String fieldName, String value,
			String allowedValues) {
		if (validateNotEmptyString(fieldName, value)) {
			if (!PathUtils.checkAllowedValues(value, allowedValues)) {
				isValid = false;
				errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
				message = String.format(
						TranscriptionErrorConstants.valueNotAllowed, fieldName);
			}
		}
	}

	public void validateStringFields(String fieldName, String value,
			int fieldSize) {
		if (validateNotEmptyString(fieldName, value)) {
			if (value.length() > fieldSize) {
				isValid = false;
				message = ValidationUtils.getFieldSizeExceededMessage(
						fieldName, ErrorConstants.fieldSizeExceeded,
						value.length(), fieldSize);
			}
		}
	}

	/**
	 * Generic validator for required string values
	 * 
	 * @param valueName
	 *            The name of the parameter
	 * @param value
	 *            The value of the parameter
	 */
	public boolean validateNotEmptyString(String valueName, String value) {
		return validateNotEmptyString(valueName, value,
				TranscriptionErrorCode.INVALID_PARAMETER,
				String.format(TranscriptionErrorConstants.emptyStringValue,
						valueName));
	}

	/**
	 * Generic validator for required string values
	 * 
	 * @param valueName
	 *            The name of the parameter
	 * @param value
	 *            The value of the parameter
	 * @param errorMessage
	 *            An optional error message
	 * @return True if the string is not empty, false otherwise along with error
	 *         settings
	 */
	public boolean validateNotEmptyString(String valueName, String value,
			TranscriptionErrorCode errorCode, String errorMessage) {
		if (value == null || value.isEmpty()) {
			isValid = false;
			message = errorMessage;
			return false;
		}
		return true;
	}

	/**
	 * Implements standard funtionality for known errors
	 * 
	 * @param errorCode
	 *            The standard error code
	 */
	public void setStandardError(TranscriptionErrorCode errorCode) {
		setIsValid(false);
		switch (errorCode) {
		case INVALID_API_CALL:
			errorCode
					.setErrorMessage(TranscriptionErrorConstants.invalidAPICall);
			break;
		case NOT_API_LOGGED_IN:
			errorCode.setErrorMessage(TranscriptionErrorConstants.notLoggedIn);
			break;
		case INVALID_PARAMETER:
			errorCode
					.setErrorMessage(TranscriptionErrorConstants.invalidParameter);
			break;
		case NO_LOCK:
			errorCode.setErrorMessage(TranscriptionErrorConstants.invalidLock);
		default:
			break;
		}
		setErrorCode(errorCode);

	}

	public boolean validatePageNumber(String pageNumber) {
		try {
			int pageNumInt = Integer.parseInt(pageNumber);

			if (pageNumInt >= 1) {
				return true;
			} else {
				isValid = false;
				message = ErrorConstants.INVALID_PAGE_NUMBER;
				return false;
			}
		} catch (Exception e) {
			return false;
		}

	}

}
