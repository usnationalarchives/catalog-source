package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component(value = "resetAccountPasswordValidator")
public class ResetAccountPasswordValidator extends VerifyAccountValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    ResetAccountPasswordRequestParameters requestParameters = (ResetAccountPasswordRequestParameters) validationResult
        .getValidatedRequest();
    UserAccountValueObject userAccount = getUserAccountDao()
        .getUserAccountByActivationCode(requestParameters.getResetCode());

    if (!validateUserAccount(validationResult, userAccount,
        requestParameters.getUserName())
        || !validateActivationCodeTimeWindow(validationResult, userAccount)) {
      return;
    }
    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }

  protected boolean validateUserAccount(ValidationResult validationResult,
      UserAccountValueObject userAccount, String userName) {
    if (userAccount == null || !userAccount.getUserName().equals(userName)) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
      error.setErrorMessage(ErrorConstants.INVALID_USER_RESET_CODE);
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }
}
