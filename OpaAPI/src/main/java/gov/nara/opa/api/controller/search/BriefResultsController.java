package gov.nara.opa.api.controller.search;

import gov.nara.opa.api.services.search.BriefResultsSearchService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.search.BriefResultsRequestParameters;
import gov.nara.opa.api.validation.search.BriefResultsValidator;
import gov.nara.opa.api.valueobject.search.BriefResultsCollectionValueObject;
import gov.nara.opa.architecture.exception.OpaApiResponseRuntimeException;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.util.LinkedHashMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Added exception handling. Upon an exception, a message is logged and an error is created.
 * The framework handles the error.
 */
@Controller
public class BriefResultsController extends AbstractBaseController {

  private static OpaLogger logger = OpaLogger
      .getLogger(BriefResultsController.class);

  @Autowired
  private BriefResultsSearchService briefResultsSearchService;

  @Autowired
  private BriefResultsValidator briefResultsValidator;

  @Autowired
  private ConfigurationService configurationService;

  public static final String BRIEF_RESULTS_ACTION = "search";
  public static final String RESULTS_PARENT_ENTITY_NAME_RESULTS = "results";

  @RequestMapping(value = { "/iapi/" + Constants.API_VERS_NUM }, method = RequestMethod.GET, params = {"action!=contentDetail"})
  public ResponseEntity<String> search(
      @Valid final BriefResultsRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    UserAccount sessionUser = SessionUtils.getSessionUser();
    String sessionAccountType = "";
    if (sessionUser != null) {
      sessionAccountType = sessionUser.getAccountType().toLowerCase();
    }
    final String accountType = sessionAccountType;
    requestParameters.setAccountType(accountType);

    ValidationResult validationResult = briefResultsValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          BRIEF_RESULTS_ACTION);
    }
    LinkedHashMap<String, Object> responseObject = null;

    // Set opaPath Variable
    final String opaPath = request.getServletPath().substring(1,
        request.getServletPath().length());

    // Construct query from request parameter
    final String query = "?" + request.getQueryString() + "&apiType=iapi";

    // Retrieve the search type
    String searchType = requestParameters.getSearchType();

    requestParameters.setHttpSessionId(request.getSession().getId());

    // Set start time
    long stTm = System.currentTimeMillis();

    try{
    responseObject = briefResultsSearchService.getBriefResults(
        requestParameters, opaPath, query, accountType);
    }catch(Exception e){
    	if(e instanceof OpaRuntimeException){
    		OpaRuntimeException oe=(OpaRuntimeException)e;
    		logger.error("caught OpaRuntimeException. Error ID="+oe.getErrorId(),e);

            logger.error("caught OpaRuntimeException. "+oe.getMessage()+" Error ID="+oe.getErrorId());
            validationResult.setValid(false);

            if(oe.getMessage().startsWith("This search has lasted more than")) {
              validationResult.setErrorCode("SEARCH_TIMEOUT");
            } else {
              validationResult.setErrorCode(ErrorCodeConstants.SEARCH_ENGINE_ERROR);
            }
            validationResult.setErrorMessage(oe.getMessage());

            return createErrorResponseEntity(validationResult, request,
                    BRIEF_RESULTS_ACTION);
    	}else{
          // handle non OpaRuntimeExceptions
          String errorId = UUID.randomUUID().toString();
          String message="caught unknown exception. Exception="+e+" Error ID="+errorId;
          logger.error(message,e);
          validationResult.setValid(false);
          validationResult.setErrorCode(ErrorCodeConstants.SEARCH_ENGINE_ERROR);
          validationResult.setErrorMessage(message);
          return createErrorResponseEntity(validationResult, request,
              BRIEF_RESULTS_ACTION);
    	}
    }

    if (responseObject == null) {
      logger.info(" ** Search Timed Out ** ");
      validationResult.setValid(false);
      validationResult.setErrorCode("SEARCH_TIMEOUT");
      validationResult.setErrorMessage(String.format("Your search timed out"));
      return createErrorResponseEntity(validationResult, request,
          BRIEF_RESULTS_ACTION);
    }

    BriefResultsCollectionValueObject brvo = (BriefResultsCollectionValueObject) responseObject
        .get("results");
    int totalBriefResults = 0;
    if (brvo != null && brvo.getTotalBriefResults() != null) {
      totalBriefResults = brvo.getTotalBriefResults();
    }

    // Set end time
    long endTm = System.currentTimeMillis();

    // Calculate the total time taken
    float duration = (endTm - stTm) / 1000f;

    logger.info(" totalTime: " + duration);

    responseObject.put("totalTime", duration);

    // Record log entry
    String logMessage = String.format(
        " Query={%1$s}, queryTime=%2$f, totalResults=%3$d, searchType=%4$s",
        query, duration, totalBriefResults, searchType);

    logger.usage(this.getClass(), ApiTypeLoggingEnum.API_TYPE_INTERNAL,
        UsageLogCode.SEARCH, logMessage);

    return createSuccessResponseEntity(requestParameters, responseObject,
        request, BRIEF_RESULTS_ACTION);
  }

}
