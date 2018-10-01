package gov.nara.opa.api.controller.search;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.export.AccountExportService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.search.ApiSearchValidator;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.search.ApiSearchRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ApiSearchController extends AbstractBaseController {
  private static OpaLogger logger = OpaLogger
      .getLogger(ApiSearchController.class);

  @Autowired
  private ApiSearchValidator apiSearchValidator;

  public static final String API_SEARCH_ACTION = "search";

  @Autowired
  AccountExportValueObjectHelper accountExportValueObjectHelper;

  @Autowired
  AccountExportService accountExportService;

  @Autowired
  ConfigurationService configurationService;

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM }, method = RequestMethod.GET)
  public ResponseEntity<String> search(
      @Valid ApiSearchRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request,
      HttpServletResponse response) {
    
    UserAccount sessionUser = SessionUtils.getSessionUser();
    String sessionAccountType = "";
    if (sessionUser != null) {
      sessionAccountType = sessionUser.getAccountType().toLowerCase();
    }
    final String accountType = sessionAccountType;
    requestParameters.setAccountType(accountType);
    
    ValidationResult validationResult = null;
    logger.trace(String.format("Starting search request with query string: %1$s", request.getQueryString()));
    requestParameters.setQueryString(request.getQueryString());

    validationResult = apiSearchValidator.validate(bindingResult, request,
        Constants.PUBLIC_API_PATH);

    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          API_SEARCH_ACTION);
    }

    long stTm = System.currentTimeMillis();
    AccountExportValueObject accountExport = accountExportValueObjectHelper
        .createAccountExportForSearch(requestParameters,
            OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
            OPAAuthenticationProvider.getUserNameForLoggedInUser(), configurationService
                .getConfig().getBulkExpDays());
    try{
    accountExport = accountExportService.createAccountExport(
        OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
        validationResult, response, accountExport,
        requestParameters.getQueryParameters(), request);
    }catch(Exception e){
		String errorId = UUID.randomUUID().toString();
		String message="Caught exception: "+e+" exception message="+e.getMessage()+ ", error Id="+errorId;
		ValidationError error = new ValidationError();
		error.setErrorCode(ErrorCodeConstants.SEARCH_ENGINE_ERROR);
		// set the default message
		error.setErrorMessage(message);
		validationResult.addCustomValidationError(error);
		logger.error(message, e);
        return createErrorResponseEntity(validationResult, request,
                API_SEARCH_ACTION);
    }
    
    long endTm = System.currentTimeMillis();
    float duration = (endTm - stTm) / 1000f;
    
    // Record log entry
    String logMessage = String.format(
        " Query={%1$s}, queryTime=%2$f, totalResults=%3$d, searchType=%4$s",
        requestParameters.getQueryString(), duration, 
        (accountExport != null ? accountExport.getTotalRecordsProcessed() : 0), 
        "API");

    logger.usage(this.getClass(), ApiTypeLoggingEnum.API_TYPE_PUBLIC,
        UsageLogCode.SEARCH, logMessage);

    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          API_SEARCH_ACTION);
    }
    return null;
  }
}
