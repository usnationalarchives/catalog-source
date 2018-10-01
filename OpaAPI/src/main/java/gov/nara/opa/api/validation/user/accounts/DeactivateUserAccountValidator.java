package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class DeactivateUserAccountValidator extends CommonUserAccountValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    DeactivateUserAccountRequestParameters requestParameters = (DeactivateUserAccountRequestParameters) validationResult
        .getValidatedRequest();
    String userName = requestParameters.getUserName();
    String password = requestParameters.getPassword();
    UserAccountValueObject userAccount = getUserAccountDao().selectByUserName(
        userName);

    if (!validateUserAccountExists(userAccount, validationResult, true)
        || !validateCurrentPassword(password, userAccount, validationResult)) {
      return;
    }
    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }

}
