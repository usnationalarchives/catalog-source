package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component(value = "setNewPasswordValidator")
public class SetNewPasswordValidator extends ResetAccountPasswordValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    SetNewPasswordRequestParameters requestParameters = (SetNewPasswordRequestParameters) validationResult
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

}
