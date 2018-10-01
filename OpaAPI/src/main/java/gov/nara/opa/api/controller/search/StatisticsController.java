package gov.nara.opa.api.controller.search;

import gov.nara.opa.api.services.search.StatisticsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.search.StatisticsRequestParameters;
import gov.nara.opa.api.validation.search.StatisticsValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class StatisticsController extends AbstractBaseController {

  @Autowired
  private StatisticsService statisticsService;

  @Autowired
  private StatisticsValidator statisticsValidator;

  public static final String STATISTICS_ACTION = "statistics";
  public static final String STATISTICS_PARENT_ENTITY_NAME_RESULTS = "results";

  @RequestMapping(value = { "/iapi/" + Constants.API_VERS_NUM + "/statistics" }, method = RequestMethod.GET)
  public ResponseEntity<String> search(
      @Valid StatisticsRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {
    ValidationResult validationResult = statisticsValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          STATISTICS_ACTION);
    }
    LinkedHashMap<String, Object> responseObject = null;
    String opaPath = request.getServletPath().substring(1,
        (request.getServletPath().length() - 11));

    requestParameters.setHttpSessionId(request.getSession().getId());
    responseObject = statisticsService
        .getStatistics(requestParameters, opaPath);
    return createSuccessResponseEntity(requestParameters, responseObject,
        request, STATISTICS_ACTION);
  }

}
