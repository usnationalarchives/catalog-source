package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class ModifyUserAccountValidator extends CommonUserAccountValidator {
  
  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    ModifyUserAccountRequestParameters requestParameters = (ModifyUserAccountRequestParameters) validationResult
        .getValidatedRequest();
    String userName = requestParameters.getUserName();
    UserAccountValueObject userAccount = getUserAccountDao().selectByUserName(
        userName);
    if (!validateUserAccountExists(userAccount, validationResult, true)
        || !validateCurrentPassword(requestParameters.getPassword(),
            userAccount, validationResult, requestParameters)
        || !validateChangeOfEmail(userAccount, validationResult,
            requestParameters.getEmail())
        || !validateIsTheSameUser(userAccount, validationResult)
        // Validation removed because of NARAOPA-637
        /*|| !validateNaraEmail(userAccount, validationResult,
            requestParameters.getEmail())*/
            ) {
      return;
    }
    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }

  protected boolean validateCurrentPassword(String providedCurrentPassword,
      UserAccountValueObject userAccount, ValidationResult validationResult,
      ModifyUserAccountRequestParameters requestParameters) {
    boolean passwordPresent = validatePasswordPresent(
        requestParameters.getEmail(), providedCurrentPassword, validationResult)
        && validatePasswordPresent(requestParameters.getFullName(),
            providedCurrentPassword, validationResult)
        && validatePasswordPresent(requestParameters.getNewPassword(),
            providedCurrentPassword, validationResult);

    if (passwordPresent) {
      if (requestParameters.getDisplayFullName() != null
          && requestParameters.getEmail() == null
          && requestParameters.getFullName() == null
          && requestParameters.getNewPassword() == null) {
        return true;
      }
      return super.validateCurrentPassword(providedCurrentPassword,
          userAccount, validationResult);
    } else {
      return false;
    }
  }

  private boolean validatePasswordPresent(Object fieldValue,
      String providedCurrentPassword, ValidationResult validationResult) {
    if (fieldValue != null && providedCurrentPassword == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.MISSING_PARAMETER);
      error
          .setErrorMessage(String.format(
              ArchitectureErrorMessageConstants.NOT_NULL_AND_NOT_EMPTY,
              "password"));
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }

}
