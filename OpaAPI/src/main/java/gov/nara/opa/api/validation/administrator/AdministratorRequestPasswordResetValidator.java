package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class AdministratorRequestPasswordResetValidator extends
    CommonUserAccountValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    String userName = ((AdministratorRequestPasswordResetRequestParameters) validationResult
        .getValidatedRequest()).getUserName();
    UserAccountValueObject userAccount = getUserAccountDao().selectByUserName(
        userName);
    if (!validateAccountExists(validationResult, userAccount)) {
      return;
    }
    if (!validateAccountIsActive(validationResult, userAccount)) {
      return;
    }
    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }

  private boolean validateAccountExists(ValidationResult validationResult,
      UserAccountValueObject userAccount) {
    if (userAccount == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
      error.setErrorMessage(ErrorConstants.INVALID_USERNAME);
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }
}
