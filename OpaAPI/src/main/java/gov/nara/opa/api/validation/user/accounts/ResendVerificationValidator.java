package gov.nara.opa.api.validation.user.accounts;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

@Component
public class ResendVerificationValidator extends CommonUserAccountValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    validateUserName(validationResult);

  }
  
  private void validateUserName(ValidationResult validationResult) {
    String userName = ((ResendVerificationRequestParameters)validationResult
        .getValidatedRequest()).getUserName();
    
    UserAccountValueObject userAccount = getUserAccountDao().selectByUserName(userName);
    if (userAccount == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
      error.setErrorMessage(ErrorConstants.USER_NAME_DOES_NOT_EXIST);
      validationResult.addCustomValidationError(error);
      return;
    }
    
    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }

}
