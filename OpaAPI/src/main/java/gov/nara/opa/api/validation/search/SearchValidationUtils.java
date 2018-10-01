package gov.nara.opa.api.validation.search;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.SearchUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;

@Component
public class SearchValidationUtils {
  
  @Autowired
  private ConfigurationService configurationService;
  
  @Autowired
  private SearchUtils searchUtils;

  public boolean isRowcountAllowed(int offset, int rows,
      ValidationResult validationResult, String itemCode) {
    return isRowcountAllowed(offset, rows, validationResult, itemCode, null);
  }
  
  public boolean isRowcountAllowed(int offset, int rows,
	      ValidationResult validationResult, String action, String accountType) {
	  return isRowcountAllowed(offset, rows, validationResult, action, accountType, false);
  }
  
  public boolean isRowcountAllowed(int offset, int rows,
      ValidationResult validationResult, String action, String accountType, boolean isPublicApi) {
    
    if(!validationResult.isValid()) {
      return false;
    }
    
    int maxRows;
    if(isPublicApi) {
    	maxRows = configurationService.getConfig().getMaxApiSearchResults();
    } else {
	    if(StringUtils.isNullOrEmtpy(accountType)) {
	      maxRows = configurationService.getConfig().getMaxSearchResultsPublic();
	    } else {
	      maxRows = configurationService.getSearchLimitForUser(accountType);
	    }
    }

    if (searchUtils.isRowcountAllowedForUser(offset, rows, accountType, maxRows, isPublicApi) ) {
      ValidationUtils.setValidationError(validationResult, ArchitectureErrorCodeConstants.RATE_LIMIT_EXCEEDED, String.format(
          ErrorConstants.MAX_LIMIT_SEARCH, maxRows), action, HttpStatus.FORBIDDEN);

      return false;
    }

    return true;
    
  }
  
  
  public boolean validateDate(String action,
      ValidationResult validationResult, Map<String, String[]> queryParams,
      String dateParamName) {

    if (queryParams.containsKey(dateParamName)) {
      String dateString = queryParams.get(dateParamName)[0];

      if (!ValidationUtils.validateSimpleDate(dateString)) {
        ValidationUtils.setValidationError(validationResult,
            ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE, String
                .format(ArchitectureErrorMessageConstants.INVALID_DATE_VALUE,
                    dateParamName), action);

        return false;
      }
    }

    return true;
  }

  
  public boolean validateDateRangeParams(ValidationResult validationResult, Map<String, String[]> queryParams, String action) {

    /*
     * f.beginDate f.endDate f.recurringDateMonth f.recurringDateDay f.exactDate
     */

    if (validationResult.isValid()) {
      if (!validateDate(action, validationResult, queryParams, "f.beginDate")) {
        return false;
      }
    }

    if (validationResult.isValid()) {
      if (!validateDate(action, validationResult, queryParams, "f.endDate")) {
        return false;
      }
    }

    if (validationResult.isValid()) {
      if (!validateDate(action, validationResult, queryParams, "f.exactDate")) {
        return false;
      }
    }

    if (validationResult.isValid()) {
      if(!validateDatePart(action, validationResult, queryParams, "f.recurringDateMonth", 1, 12));
    }

    if (validationResult.isValid()) {
      if(!validateDatePart(action, validationResult, queryParams, "f.recurringDateDay", 1, 31));
    }
    
    return true;

  }

  public boolean validateDatePart(String action,
      ValidationResult validationResult, Map<String, String[]> queryParams,
      String dateParamName, int minValue, int maxValue) {

    if (queryParams.containsKey(dateParamName)) {
      String datePartString = queryParams.get(dateParamName)[0];

      if (!ValidationUtils.validateInt(datePartString)
          || !ValidationUtils.validateInclusiveRange(Integer.parseInt(datePartString),
              minValue, maxValue)) {
        ValidationUtils.setValidationError(validationResult,
            ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE, String
                .format(
                    ArchitectureErrorMessageConstants.INVALID_DATE_PART_VALUE,
                    dateParamName, minValue, maxValue), action);

        return false;
      }
    }

    return true;
  }
  
  
}
