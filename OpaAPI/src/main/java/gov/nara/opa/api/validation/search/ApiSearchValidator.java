package gov.nara.opa.api.validation.search;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import gov.nara.opa.architecture.logging.OpaLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.export.CreateAccountExportValidator;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.services.SingletonServices;
import gov.nara.opa.common.validation.search.ApiSearchRequestParameters;
import gov.nara.opa.common.validation.search.ResultTypesValidator;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;
/**
 * added cursor mark validation
 */
@Component
public class ApiSearchValidator extends OpaApiAbstractValidator {
	
	private static OpaLogger logger = OpaLogger.getLogger(ApiSearchValidator.class);
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ResultTypesValidator resultTypesValidator;

	@Autowired
	private SearchValidationUtils searchValidationUtils;

	@Autowired
	private SolrUtils solrUtils;

	private static final List<String> allowedFormats = new ArrayList<String>();

	public static final String SEARCH_ACTION = "search";

	static {
		allowedFormats.add("xml");
		allowedFormats.add("json");
		allowedFormats.add("pdf");
		allowedFormats.add("csv");
		allowedFormats.add("txt");
	}

	static OpaLogger log = OpaLogger
			.getLogger(ApiSearchValidator.class);
	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {

		String cursorMarkValue="";
		// check the cursor mark and add a validation error if invalid
		boolean validCursorMark=validateCursorMark(validationResult, request.getParameterMap());

		if(!validCursorMark){
			// simply return. 
			logger.error("performCustomValidation: returning error due to INVALID cursorMarkValue="+cursorMarkValue);
			return;
		}
		ApiSearchRequestParameters requestParameters = (ApiSearchRequestParameters) validationResult
				.getValidatedRequest();

		//Format param must be checked first due to additional format values
		validateFormatParameter(validationResult, requestParameters);
		if(!validationResult.isValid()) {
			//Check for rate limit errors and set Http status to 403
			for(ValidationError error : validationResult.getErrors()) {
				if(error.getErrorCode().equals(ArchitectureErrorCodeConstants.RATE_LIMIT_EXCEEDED)) {
					validationResult.setHttpStatus(HttpStatus.FORBIDDEN);
				}
			}

		} else {

			String query;
			if (request.getQueryString() == null) {
				query = "?action=search";
			} else {
				query = "?" + request.getQueryString();
			}

			Map<String, String[]> paramMap = solrUtils.makeParamMap(query);

			requestParameters.setQueryParameters(AccountExportValueObjectHelper
					.scrubQueryParameters(paramMap,
							requestParameters.getApiType()));

			if(validateResultFieldsParam(validationResult, requestParameters)
					&& validateResultTypesParam(validationResult, requestParameters)
					&& validateMaxRowsLimit(validationResult, requestParameters)
					&& validateCursorMarkOffsetExclusivity(validationResult, requestParameters)
					&& searchValidationUtils.validateDateRangeParams(validationResult, requestParameters.getQueryParameters(), SEARCH_ACTION)) {
				validationResult.addContextObject(
						CreateAccountExportValidator.TOTAL_NO_OF_ESTIMATED_RECORDS,
						new Integer(requestParameters.getRows()));
			}
		}
	}
	
	/**
	 * validate the cursor
	 * 
	 * @param validationResult
	 *            - output param. this will add an error to validationResult's
	 *            errors
	 * @param requestParameter
	 *            - the map of request parameters from the query string of the
	 *            request
	 * @return true if the cursor mark is valid or if cursor mark is NOT
	 *         present. returns false otherwise
	 */
	private boolean validateCursorMark(ValidationResult validationResult, Map<String, String[]> requestParameters) {
		String[] cvalues = requestParameters.get("cursorMark");
		boolean validCursorMark = true;
		String cursorMarkValue = "";
		if (cvalues != null) {
			cursorMarkValue = cvalues[0];
			if (cursorMarkValue.toLowerCase().contains("\\")) {
				cursorMarkValue = cursorMarkValue.replaceAll("\\\\", "");
			}
			// handle "+" characters
			cursorMarkValue = cursorMarkValue.replaceAll("\\s{1}", "+");
			logger.debug("cursorMarkValue=" + cursorMarkValue);
			Pattern p = Pattern.compile("^([a-zA-Z0-9+/]+)(=+){0,1}(-\\d+){0,1}$");
			Matcher m = p.matcher(cursorMarkValue);
			// include * as solr accepts this
			if (m.find() || cursorMarkValue.equals("*")) {
				validCursorMark = true;
			}else{
				validCursorMark=false;
			}
		}
		if (!validCursorMark) {
			ValidationError error = new ValidationError();
			String errorId = UUID.randomUUID().toString();
			error.setErrorMessage(ErrorConstants.INVALID_CURSOR_MARK + " Error Id=" + errorId);
			// log the error id.
			logger.error("invalid cursor mark. error id=" + errorId);
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
			validationResult.setValid(false);
			validationResult.getErrors().add(error);
		}
		return validCursorMark;
	}
	private void validateFormatParameter(ValidationResult validationResult,
			ApiSearchRequestParameters requestParameters) {
		List<ValidationError> newErrors = new ArrayList<ValidationError>();

		if (!validationResult.isValid()) {
			for (ValidationError error : validationResult.getErrors()) {
				if (!ArchitectureErrorMessageConstants.INVALID_FORMAT.equals(error
						.getErrorMessage())) {
					newErrors.add(error);
				}
			}

			validationResult.getErrors().clear();
			if (newErrors.size() > 0) {
				validationResult.getErrors().addAll(newErrors);
			} else {
				validationResult.setValid(true);
			}
		}

		if (!allowedFormats.contains(requestParameters.getFormat())) {
			ValidationError error = new ValidationError();
			error
			.setErrorMessage(ArchitectureErrorMessageConstants.INVALID_FORMAT_EXPORT);
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PATTERN);
			validationResult.addCustomValidationError(error);
		}

	}

	private boolean validateMaxRowsLimit(ValidationResult validationResult, ApiSearchRequestParameters requestParameters) {
		int offset = requestParameters.getOffset();
		int rows = requestParameters.getRows();

		return searchValidationUtils.isRowcountAllowed(offset, rows, validationResult, SEARCH_ACTION, requestParameters.getAccountType(), true);

	}

	private boolean validateResultFieldsParam(ValidationResult validationResult, ApiSearchRequestParameters requestParameters) {
		Map<String, String[]> queryParams = requestParameters.getQueryParameters();


		if(queryParams.containsKey("resultFields")) {
			for(String fieldNameString : queryParams.get("resultFields")) {
				String[] fieldNames = fieldNameString.split(",");

				for(String fieldName : fieldNames) {
					String trimmedFieldName = fieldName.trim();
					if  (!SingletonServices.SOLR_RESULT_FIELDS_WITH_LIST.contains(trimmedFieldName)
							&& !SingletonServices.DAS_WHITE_LIST.contains(trimmedFieldName)) {
						ValidationError error = new ValidationError();
						error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_FIELD);
						error.setErrorMessage(String.format(
								ErrorConstants.INVALID_SEARCH_RESULTS_FIELD,
								trimmedFieldName));
						validationResult.addCustomValidationError(error);

						return false;
					}
				}
			}
		}

		return true;
	}

    private boolean validateCursorMarkOffsetExclusivity(ValidationResult validationResult, ApiSearchRequestParameters requestParameters) {
        Map<String, String[]> queryParams = requestParameters.getQueryParameters();

        if(requestParameters.getAspireObjectContent("").containsKey("cursorMark") && 
                requestParameters.getAspireObjectContent("").containsKey("offset")) {
            ValidationError error = new ValidationError();
            error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
            error.setErrorMessage(ErrorConstants.INVALID_OFFSET_CURSORMARK);
            validationResult.addCustomValidationError(error);
            return false;
        }
        return true;
    }
	
	
	private boolean validateResultTypesParam(ValidationResult validationResult, ApiSearchRequestParameters requestParameters) {
		Map<String, String[]> queryParams = requestParameters.getQueryParameters();

		if(queryParams.containsKey("resultTypes")) {
			for(String typeNameString : queryParams.get("resultTypes")) {
				String[] typeNames = typeNameString.split(",");

				for(String typeName : typeNames) {
					String trimmedTypeName = typeName.trim();

					if (!resultTypesValidator.isValid(trimmedTypeName)) {
						ValidationError error = new ValidationError();
						error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_RESULT_TYPE);
						error.setErrorMessage(String.format(
								ErrorConstants.INVALID_SEARCH_RESULTS_TYPE,
								trimmedTypeName));
						validationResult.addCustomValidationError(error);

						return false;
					}
				}
			}
		}

		return true;
	}


}
