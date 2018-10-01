package gov.nara.opa.api.validation;

import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;

public class ValidationUtils {

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
	public static String getFieldSizeExceededMessage(String fieldName,
			String errorMessage, int currentLength, int limit) {
		return String.format(errorMessage, fieldName, currentLength, limit);
	}

	/**
	 * Validates that no bad parameters are passed to an API method
	 * 
	 * @param validParameterNames
	 *            Map of valid parameter names
	 * @param requestParameterStr
	 *            String of the request parameters
	 * @return
	 */

	public static boolean validateRequestParameterNames(
			LinkedHashMap<String, String> validParameterNames,
			String requestParameterStr) {
		if (!StringUtils.isNullOrEmtpy(requestParameterStr)) {
			String[] splitParameterString = requestParameterStr.split("&");
			for (int x = 0; x < splitParameterString.length; x++) {
				String requestParam = (String) splitParameterString[x].trim();
				String[] splitRequestParam = requestParam.split("=");
				if (splitRequestParam.length > 0) {
					if (!validParameterNames.containsKey(splitRequestParam[0]
							.trim())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static boolean validateRequestParameterNames(
			LinkedHashMap<String, String> validParameterNames,
			Iterator<String> requestParameterIterator) {
		while (requestParameterIterator.hasNext()) {
			if (!validParameterNames.containsKey(requestParameterIterator
					.next())) {
				return false;
			}
		}
		return true;
	}

	public static void setValidationError(ValidationResult validationResult,
			String errorCode, String errorMessage, String action) {
		setValidationError(validationResult, errorCode, errorMessage, action,
				HttpStatus.BAD_REQUEST);
	}

	public static void setValidationError(ValidationResult validationResult,
			String errorCode, String errorMessage, String action,
			HttpStatus status) {
		ValidationError validationError = new ValidationError();
		validationError.setErrorCode(errorCode);
		validationError.setErrorMessage(errorMessage);
		validationError.setFieldValidationError(true);
		validationError.setValidatedItemCode(action);
		validationResult.setHttpStatus(status);
		validationResult.addCustomValidationError(validationError);
	}

	public static boolean validateSimpleDate(String dateString) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			format.parse(dateString);
		} catch(Exception pe) {
			return false;
		}
		return true;
	}

	public static boolean validateInt(String intString) {
		return intString.matches("\\d+");
	}

	public static boolean validateInclusiveRange(int value, int minValue, int maxValue) {
		return (minValue <= value && value <= maxValue);
	}
}
