package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.dataaccess.administrator.AccountReasonDao;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;
import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddAccountReasonValidator extends AbstractBaseValidator {

  @Autowired
  AccountReasonDao accountReasonsDao;

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    AddAccountReasonRequestParameters requestParameters = (AddAccountReasonRequestParameters) validationResult
        .getValidatedRequest();
    List<AccountReasonValueObject> accountReasons = accountReasonsDao
        .getAcountReasons(null, requestParameters.getText(), true);
    validateAccountReasonDoesNotExist(accountReasons, validationResult);
  }

  private void validateAccountReasonDoesNotExist(
      List<AccountReasonValueObject> accountReasons,
      ValidationResult validationResult) {
    if (accountReasons != null && accountReasons.size() > 0) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.DUPLICATE_REASON);
      error
          .setErrorMessage(ErrorConstants.ACTIVE_ACCOUNT_REASON_ALREADY_EXISTS);
      validationResult.addCustomValidationError(error);
    }
  }

}
