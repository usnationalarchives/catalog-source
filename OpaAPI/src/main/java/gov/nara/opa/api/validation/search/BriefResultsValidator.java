package gov.nara.opa.api.validation.search;

import java.util.LinkedHashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.services.SingletonServices;
import gov.nara.opa.common.validation.search.ResultTypesValidator;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;

@Component
public class BriefResultsValidator extends OpaApiAbstractValidator {

  public static final String BRIEF_RESULTS_ACTION = "search";

  private static final LinkedHashSet<String> orderedValidatedItemCodes = new LinkedHashSet<String>();

  @Autowired
  private ResultTypesValidator resultTypesValidator;

  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private SearchValidationUtils searchValidationUtils;

  static {
    orderedValidatedItemCodes.add("action");
    orderedValidatedItemCodes.add("q");
    orderedValidatedItemCodes.add("offset");
    orderedValidatedItemCodes.add("rows");
  }

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    BriefResultsRequestParameters briefResultsRequestParameters = (BriefResultsRequestParameters) validationResult
        .getValidatedRequest();
    if (validationResult.isValid()) {
      String action = briefResultsRequestParameters.getAction();

      validateAction(briefResultsRequestParameters.getAction(),
          validationResult);

      validateOffset(briefResultsRequestParameters.getOffset(),
          validationResult, briefResultsRequestParameters.getAccountType());
      validateRows(briefResultsRequestParameters.getRows(), validationResult);
      validateMaxRowsLimit(briefResultsRequestParameters.getOffset(),
          briefResultsRequestParameters.getRows(), validationResult,
          briefResultsRequestParameters.getAccountType());

      Map<String, String[]> queryParams = AccountExportValueObjectHelper
          .scrubQueryParameters(request.getParameterMap(),
              Constants.INTERNAL_API_PATH);
      validateResultFieldsParam(validationResult, queryParams);

      validateResultTypesParam(validationResult, queryParams);
      
      validateQParam(validationResult, action);

      searchValidationUtils.validateDateRangeParams(validationResult, queryParams, action);
    }
  }



  @Override
  protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
    return orderedValidatedItemCodes;
  }

  private boolean validateAction(String action,
      ValidationResult validationResult) {
    if ((action != null)
        && (!action.equalsIgnoreCase("search")
            && !action.equalsIgnoreCase("searchWithin") && !action
              .equalsIgnoreCase("searchTag"))) {
      ValidationError validationError = new ValidationError();
      validationError
          .setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
      validationError.setErrorMessage(String.format(
          ErrorConstants.INVALID_ACTION, action));
      validationError.setFieldValidationError(true);
      validationError.setValidatedItemCode(BRIEF_RESULTS_ACTION);
      validationResult.addCustomValidationError(validationError);
      return false;
    }
    return true;
  }

  private boolean validateOffset(int offset, ValidationResult validationResult,
      String accountType) {

    if (offset < 0) {
      ValidationError validationError = new ValidationError();
      validationError
          .setErrorCode(ArchitectureErrorCodeConstants.INVALID_OFFSET_LIMIT);
      validationError.setErrorMessage(String.format(
          ErrorConstants.INVALID_ACTION, offset));
      validationError.setFieldValidationError(true);
      validationError.setValidatedItemCode(BRIEF_RESULTS_ACTION);
      validationResult.addCustomValidationError(validationError);
      return false;
    }

    int maxOffset = configurationService.getSearchLimitForUser(accountType);

    if (offset >= maxOffset) {
      ValidationError validationError = new ValidationError();
      validationError
          .setErrorCode(ArchitectureErrorCodeConstants.INVALID_OFFSET_LIMIT);
      validationError.setErrorMessage(String.format(
          ErrorConstants.INVALID_OFFSET_UPPER_LIMIT, maxOffset));
      validationError.setFieldValidationError(true);
      validationError.setValidatedItemCode(BRIEF_RESULTS_ACTION);
      validationResult.addCustomValidationError(validationError);
      return false;
    }

    return true;
  }

  private boolean validateRows(int rows, ValidationResult validationResult) {
    if (rows < 0) {
      ValidationError validationError = new ValidationError();
      validationError
          .setErrorCode(ArchitectureErrorCodeConstants.INVALID_ROWS_VALUE);
      validationError.setErrorMessage(String.format(
          ErrorConstants.INVALID_ACTION, rows));
      validationError.setFieldValidationError(true);
      validationError.setValidatedItemCode(BRIEF_RESULTS_ACTION);
      validationResult.addCustomValidationError(validationError);
      return false;
    }
    return true;
  }

  private boolean validateMaxRowsLimit(int offset, int rows,
      ValidationResult validationResult, String accountType) {

    return searchValidationUtils.isRowcountAllowed(offset, rows,
        validationResult, BRIEF_RESULTS_ACTION, accountType);

  }

  private boolean validateResultFieldsParam(ValidationResult validationResult,
      Map<String, String[]> queryParams) {

    if (queryParams.containsKey("resultFields")) {
      for (String fieldNameString : queryParams.get("resultFields")) {
        String[] fieldNames = fieldNameString.split(",");

        for (String fieldName : fieldNames) {
          String trimmedFieldName = fieldName.trim();

          if (!SingletonServices.SOLR_RESULT_FIELDS_WITH_LIST
              .contains(trimmedFieldName)
              && !SingletonServices.DAS_WHITE_LIST.contains(trimmedFieldName)) {
            ValidationError error = new ValidationError();
            error
                .setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
            error.setErrorMessage(String.format(
                ErrorConstants.INVALID_SEARCH_RESULTS_FIELD, trimmedFieldName));
            validationResult.addCustomValidationError(error);

            return false;
          }
        }
      }
    }

    return true;
  }

  private boolean validateResultTypesParam(ValidationResult validationResult,
      Map<String, String[]> queryParams) {

    if (queryParams.containsKey("resultTypes")) {
      for (String typeNameString : queryParams.get("resultTypes")) {
        String[] typeNames = typeNameString.split(",");

        for (String typeName : typeNames) {
          String trimmedTypeName = typeName.trim();

          if (!resultTypesValidator.isValid(trimmedTypeName)) {
            ValidationError error = new ValidationError();
            error
                .setErrorCode(ArchitectureErrorCodeConstants.INVALID_RESULT_TYPE);
            error.setErrorMessage(String.format(
                ErrorConstants.INVALID_SEARCH_RESULTS_TYPE, trimmedTypeName));
            validationResult.addCustomValidationError(error);

            return false;
          }
        }
      }
    }

    return true;
  }
  
  
  private boolean validateQParam(ValidationResult validationResult, String action) {
	    BriefResultsRequestParameters briefResultsRequestParameters = (BriefResultsRequestParameters) validationResult
	            .getValidatedRequest();
	    String q = briefResultsRequestParameters.getQ();
	    String validQRegex = configurationService.getConfig().getInvalidSearchStringPattern();
	    
	    //Apply regex to q
	    if(q.matches(validQRegex)) {
	    	ValidationUtils.setValidationError(validationResult, 
	    			ArchitectureErrorCodeConstants.INVALID_PARAMETER, 
	    			String.format(ErrorConstants.INVALID_SEARCH_QUERY, q), 
	    			action);

	    	return false;
	    }

	    
	    return true;
  }

}
