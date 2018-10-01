package gov.nara.opa.api.validation.print;

import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.export.CreateAccountExportValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.print.PrintResultsRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;

@Component
public class PrintResultsValidator extends OpaApiAbstractValidator {

  public static final String PRINT_RESULTS_ACTION = "print";

  @Autowired
  private ConfigurationService configurationService;

  private static final LinkedHashSet<String> orderedValidatedItemCodes = new LinkedHashSet<String>();

  static {
    orderedValidatedItemCodes.add("action");
    orderedValidatedItemCodes.add("q");
    orderedValidatedItemCodes.add("printType");
  }

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    PrintResultsRequestParameters printResultsRequestParameters = (PrintResultsRequestParameters) validationResult
        .getValidatedRequest();
    printResultsRequestParameters
        .setQueryParameters(AccountExportValueObjectHelper
            .scrubQueryParameters(request.getParameterMap(),
                printResultsRequestParameters.getApiType()));

    validationResult.addContextObject(
        CreateAccountExportValidator.TOTAL_NO_OF_ESTIMATED_RECORDS,
        printResultsRequestParameters.getRows());

    validatePrintType(printResultsRequestParameters.getPrintType(),
        validationResult);
    validateOffset(printResultsRequestParameters.getOffset(), validationResult);
    validateMaxRowsLimit(printResultsRequestParameters.getOffset(),
        printResultsRequestParameters.getRows(), validationResult);
  }

  private boolean validatePrintType(String printType,
      ValidationResult validationResult) {
    if ((printType != null)
        && ((!printType.equalsIgnoreCase("brief")) && (!printType
            .equalsIgnoreCase("full")))) {
      ValidationError validationError = new ValidationError();
      validationError
          .setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
      validationError.setErrorMessage(String.format(
          ErrorConstants.INVALID_PRINT_TYPE, printType));
      validationError.setFieldValidationError(true);
      validationError.setValidatedItemCode(PRINT_RESULTS_ACTION);
      validationResult.addCustomValidationError(validationError);
      return false;
    }
    return true;
  }

  private boolean validateOffset(int offset, ValidationResult validationResult) {
    if (offset < 0) {
      ValidationError validationError = new ValidationError();
      validationError
          .setErrorCode(ArchitectureErrorCodeConstants.INVALID_OFFSET_LIMIT);
      validationError.setErrorMessage(String.format(
          ErrorConstants.INVALID_ACTION, offset));
      validationError.setFieldValidationError(true);
      validationError.setValidatedItemCode(PRINT_RESULTS_ACTION);
      validationResult.addCustomValidationError(validationError);
      return false;
    }
    return true;
  }

  private boolean validateMaxRowsLimit(int offset, int rows,
      ValidationResult validationResult) {

    int maxRows = getLimitForUser();

    if ((offset + rows) > maxRows) {
      ValidationError validationError = new ValidationError();
      validationError
          .setErrorCode(ArchitectureErrorCodeConstants.RATE_LIMIT_EXCEEDED);
      validationError.setErrorMessage(String.format(
          ErrorConstants.MAX_LIMIT_SEARCH, maxRows));
      validationError.setFieldValidationError(true);
      validationError.setValidatedItemCode(PRINT_RESULTS_ACTION);
      validationResult.addCustomValidationError(validationError);
      return false;
    }

    return true;
  }

  private int getLimitForUser() {

    UserAccount sessionUser = SessionUtils.getSessionUser();

    if (sessionUser != null) {
      switch (sessionUser.getAccountType().toLowerCase()) {
        case "standard":
          return configurationService.getConfig().getMaxSearchResultsStandard();
        case "power":
          return configurationService.getConfig().getMaxSearchResultsPower();
      }

    } else {
      return configurationService.getConfig().getMaxSearchResultsPublic();
    }

    return 0;
  }

  @Override
  protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
    return orderedValidatedItemCodes;
  }

}
