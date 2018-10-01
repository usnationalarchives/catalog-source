package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdministratorModifyAccountValidator extends
    CommonUserAccountValidator {
  
  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    AdministratorModifyAccountRequestParameters requestParameters = (AdministratorModifyAccountRequestParameters) validationResult
        .getValidatedRequest();
    String userName = requestParameters.getUserName();
    UserAccountValueObject userAccount = getUserAccountDao().selectByUserName(
        userName);
    if (!validateUserAccountExists(userAccount, validationResult, null)
        || !validatePasswordChange(validationResult, userAccount,
            requestParameters)
        || !validateChangeOfEmail(userAccount, validationResult,
            requestParameters.getEmail())
            ) {
      return;
    }
    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }

  private boolean validatePasswordChange(ValidationResult validationResult,
      UserAccountValueObject userAccount,
      AdministratorModifyAccountRequestParameters requestParameters) {
    String newPassword = requestParameters.getNewPassword();
    if (newPassword == null) {
      return true;
    }

    if (requestParameters.getPassword() == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.CHANGE_PASSWORD);
      error
          .setErrorMessage(ErrorConstants.CHANGE_PASSWORD_BOTH_OLD_AND_NEW_PRESENT);
      validationResult.addCustomValidationError(error);
      return false;
    }

    return validateCurrentPassword(requestParameters.getPassword(),
        userAccount, validationResult);
  }

}
