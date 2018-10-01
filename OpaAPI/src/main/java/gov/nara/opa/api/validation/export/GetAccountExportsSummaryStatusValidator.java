package gov.nara.opa.api.validation.export;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GetAccountExportsSummaryStatusValidator extends
    AbstractBaseValidator {

  @Autowired
  AccountExportDao accountExportDao;

  public static final String ACCOUNT_EXPORTS_SUMMARY_STATUS = "accountExportsSummaryStatus";

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {

    LinkedHashMap<String, Object> summaryStatus = accountExportDao
        .getStatusSummary(OPAAuthenticationProvider
            .getAccountIdForLoggedInUser());

    if (summaryStatus == null || summaryStatus.size() == 0) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.EXPORT_NOT_FOUND);
      error.setErrorMessage(ErrorConstants.EXPORT_NOT_FOUND);
      validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
      validationResult.addCustomValidationError(error);
      return;
    }
    validationResult.addContextObject(ACCOUNT_EXPORTS_SUMMARY_STATUS,
        summaryStatus);
  }
}
