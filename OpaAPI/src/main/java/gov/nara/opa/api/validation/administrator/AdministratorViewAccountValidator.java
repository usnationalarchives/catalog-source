package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class AdministratorViewAccountValidator extends
    CommonUserAccountValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    AdministratorViewAccountRequestParameters requestParameters = (AdministratorViewAccountRequestParameters) validationResult
        .getValidatedRequest();
    String userName = requestParameters.getUserName();
    UserAccountValueObject userAccount = getUserAccountDao().selectByUserName(
        userName);
    if (!validateUserAccountExists(userAccount, validationResult, true)) {
      return;
    }

    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }
}
