package gov.nara.opa.api.controller.search;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nara.opa.api.services.search.ContentDetailService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.api.validation.search.PagedContentDetailRequestParameters;
import gov.nara.opa.api.validation.search.PagedContentDetailValidator;
import gov.nara.opa.api.valueobject.search.ContentDetailValueObject;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;

@Controller
public class PagedContentDetailController extends AbstractBaseController {
  
  private static OpaLogger logger = OpaLogger
      .getLogger(PagedContentDetailController.class);
  
  @Autowired
  private PagedContentDetailValidator pagedContentDetailValidator;
  
  @Autowired
  private ContentDetailService contentDetailService;

  public static final String PAGED_CONTENT_DETAIL_ACTION = "contentDetail";
  
  @RequestMapping(value = { "/iapi/" + Constants.API_VERS_NUM }, method = RequestMethod.GET, params = "action=contentDetail")
  public ResponseEntity<String> getContentDetail(
      @Valid PagedContentDetailRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {
    
    UserAccount sessionUser = SessionUtils.getSessionUser();
    String sessionAccountType = "";
    if (sessionUser != null) {
      sessionAccountType = sessionUser.getAccountType().toLowerCase();
    }
    final String accountType = sessionAccountType;
    requestParameters.setAccountType(accountType);
    
    ValidationResult validationResult = pagedContentDetailValidator.validate(
        bindingResult, request);
    
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          PAGED_CONTENT_DETAIL_ACTION);
    }
    
    ContentDetailValueObject responseObject = new ContentDetailValueObject();
    
    // Construct query from request parameter
    String query = "?" + request.getQueryString() + "&apiType=iapi";

    
    // Set opaPath Variable
    String opaPath = requestParameters.getApiType() + "/"
        + Constants.API_VERS_NUM;
    
    requestParameters.setHttpSessionId(request.getSession().getId());

    responseObject = contentDetailService.getContentDetail(requestParameters,
        opaPath, query);
    String sourceType = responseObject.getSourceType();

    // Record log entry
    String logMessage = String.format(
        "Query={%1$s}, Source={%2$s}, NaId={%3$s}", query, sourceType, responseObject.getNaId());
    logger.usage(this.getClass(), ApiTypeLoggingEnum.API_TYPE_INTERNAL,
        UsageLogCode.SEARCH, logMessage);
    logger.info(logMessage);

    return createSuccessResponseEntity(requestParameters, responseObject,
        request, PAGED_CONTENT_DETAIL_ACTION);
    
    
  }

}
