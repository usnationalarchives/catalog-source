package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class ForgotUserNameValidator extends CommonUserAccountValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    String email = ((ForgotUserNameRequestParameters) validationResult
        .getValidatedRequest()).getEmail();
    UserAccountValueObject userAccount = getUserAccountDao().selectByEmail(
        email);
    if (!validateAccountExists(validationResult, userAccount)) {
      return;
    }
    if (!validateAccountIsActive(validationResult, userAccount)) {
      return;
    }
    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }

  protected boolean validateAccountExists(ValidationResult validationResult,
      UserAccountValueObject userAccount) {
    if (userAccount == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
      error.setErrorMessage(ErrorConstants.EMAIL_NOT_EXISTS);
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }

}
