package gov.nara.opa.api.controller.search;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.search.ContentDetailService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.search.ContentDetailRequestParameters;
import gov.nara.opa.api.validation.search.ContentDetailValidator;
import gov.nara.opa.api.valueobject.search.ContentDetailValueObject;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ContentDetailController extends AbstractBaseController {

  private static OpaLogger logger = OpaLogger
      .getLogger(ContentDetailController.class);

  @Autowired
  private ContentDetailService contentDetailService;

  @Autowired
  private ContentDetailValidator contentDetailValidator;

  @Autowired
  APIResponse apiResponse;

  public static final String CONTENT_DETAIL_ACTION = "content";

  @SuppressWarnings("deprecation")
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/id/{naId}" }, method = RequestMethod.GET)
  public ResponseEntity<String> search(
      @Valid ContentDetailRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {
    ValidationResult validationResult = contentDetailValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          CONTENT_DETAIL_ACTION);
    }
    // AbstractWebEntityValueObject responseObject = null;
    ContentDetailValueObject responseObject = new ContentDetailValueObject();

    // Set opaPath Variable
    String opaPath = requestParameters.getApiType() + "/"
        + Constants.API_VERS_NUM;

    // Retrieve the naId from the request parameters
    String naId = requestParameters.getNaId();

    String query = "?contentDetailID=" + naId;

    // For User-Interface - Retrieve the search term for highlighting
    String searchTerm = requestParameters.getSearchTerm();
    if (searchTerm != null && !searchTerm.equals("")) {

      logger.info("Query Before Encoding:    " + query);
      logger.info("Search-Term Before Encoding:    " + searchTerm);

      query = query + "&q=" + URLEncoder.encode(searchTerm);

      logger.info("Query + Search-Term After Encoding:    " + query);

    }

    requestParameters.setHttpSessionId(request.getSession().getId());

    responseObject = contentDetailService.getContentDetail(requestParameters,
        opaPath, query);
    String sourceType = responseObject.getSourceType();

    // Record log entry
    String logMessage = String.format(
        "Query={%1$s}, Source={%2$s}, NaId={%3$s}", query, sourceType, naId);
    logger.usage(this.getClass(), ApiTypeLoggingEnum.API_TYPE_INTERNAL,
        UsageLogCode.SEARCH, logMessage);
    logger.info(logMessage);

    return createSuccessResponseEntity(requestParameters, responseObject,
        request, CONTENT_DETAIL_ACTION);
  }

}
