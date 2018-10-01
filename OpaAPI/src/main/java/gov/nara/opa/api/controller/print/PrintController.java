package gov.nara.opa.api.controller.print;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.export.AccountExportService;
import gov.nara.opa.api.services.print.PrintResultsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.print.PrintResultsValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.print.PrintResultsRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PrintController extends AbstractBaseController {

  private static OpaLogger logger = OpaLogger.getLogger(PrintController.class);

  public static final String ANONYMOUS_USER_NAME = "Anonymous";

  @Autowired
  private PrintResultsService printResultsService;

  @Autowired
  private PrintResultsValidator printResultsValidator;

  @Autowired
  AccountExportValueObjectHelper accountExportHelper;

  @Autowired
  AccountExportService accountExportService;

  public static final String PRINT_RESULTS_ACTION = "print";
  public static final String RESULTS_PARENT_ENTITY_NAME_RESULTS = "results";

  @RequestMapping(value = { "/iapi/" + Constants.API_VERS_NUM + "/print" }, method = RequestMethod.GET)
  public ResponseEntity<String> search(
      @Valid PrintResultsRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request,
      HttpServletResponse response) {
    ValidationResult validationResult = printResultsValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          PRINT_RESULTS_ACTION);
    }

    // Set opaPath Variable
    // String opaPath = requestParameters.getApiType() + "/"
    // + Constants.API_VERS_NUM;

    // Contruct query from request parameter
    String query = "?" + request.getQueryString() + "&apiType=iapi";

    // Retrieve the print type
    String printType = requestParameters.getPrintType();

    requestParameters.setHttpSessionId(request.getSession().getId());

    // Retrieve the accountId (for logged-in users)
    Integer accountId = OPAAuthenticationProvider.getAccountIdForLoggedInUser();

    // Retrieve the userName (for logged-in users)
    String userName = accountId == null ? ANONYMOUS_USER_NAME
        : OPAAuthenticationProvider.getUserNameForLoggedInUser();

    // Create the AccountExportValueObject
    AccountExportValueObject accountExport = accountExportHelper
        .createAccountExportForPrint(requestParameters, accountId, userName,
            requestParameters.getQueryParameters());

    // Retrieve the print results
    accountExport = accountExportService.createAccountExport(accountId,
        validationResult, response, accountExport,
        requestParameters.getQueryParameters(), request);

    // Record log entry
    String logMessage = String.format(
        " Query={%1$s}, queryTime=%2$f, totalResults=%3$d, searchType=%4$s",
        query, 0.00, 0, printType);
    logger.info(logMessage);
    return null;
    // return createSuccessResponseEntity(requestParameters, accountExport,
    // request, PRINT_RESULTS_ACTION);
  }
}
