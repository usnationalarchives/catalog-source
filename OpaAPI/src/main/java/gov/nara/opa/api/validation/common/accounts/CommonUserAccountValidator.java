package gov.nara.opa.api.validation.common.accounts;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.services.user.accounts.PasswordUserAccountService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.system.OpaErrorCodeConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public abstract class CommonUserAccountValidator extends
    OpaApiAbstractValidator {

  public static final String USER_ACCOUNT_OBJECT_KEY = "userAccount";

  @Autowired
  private ConfigurationService configService;
  
  @Autowired
  private UserAccountDao userAccountDao;

  @Autowired
  PasswordUserAccountService passwordAccountService;

  public UserAccountDao getUserAccountDao() {
    return userAccountDao;
  }

  public void setUserAccountDao(UserAccountDao userAccountDao) {
    this.userAccountDao = userAccountDao;
  }

  @Override
  protected abstract void performCustomValidation(
      ValidationResult validationResult, HttpServletRequest request);

  protected boolean validateAccountIsActive(ValidationResult validationResult,
      UserAccountValueObject userAccount) {
    if (!userAccount.getAccountStatus()) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.ACCOUNT_INACTIVE);
      error.setErrorMessage(ErrorConstants.ACCOUNT_INACTIVE);
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }

  protected boolean validateUserAccountExists(
      UserAccountValueObject userAccount, ValidationResult validationResult,
      Boolean status, String errorMessage) {
    if (userAccount == null
        || (status != null && !userAccount.getAccountStatus().equals(status))) {
      ValidationError validationError = new ValidationError();
      validationError.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
      if (errorMessage == null) {
        validationError
            .setErrorMessage(ErrorConstants.USER_NAME_DOES_NOT_EXIST);
      } else {
        validationError.setErrorMessage(errorMessage);
      }

      validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
      validationResult.addCustomValidationError(validationError);
      return false;
    }
    return true;
  }

  protected boolean validateUserAccountExists(
      UserAccountValueObject userAccount, ValidationResult validationResult,
      Boolean status) {
    return validateUserAccountExists(userAccount, validationResult, status,
        null);
  }

  protected boolean validateCurrentPassword(String providedCurrentPassword,
      UserAccountValueObject userAccount, ValidationResult validationResult) {
    if (!passwordAccountService.passwordMatches(userAccount,
        providedCurrentPassword)) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.BAD_PASSWORD);
      error.setErrorMessage(ArchitectureErrorMessageConstants.WRONG_PASSWORD);
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }

  protected boolean validateIsTheSameUser(UserAccountValueObject userAccount,
      ValidationResult validationResult) {
    if (!userAccount.getAccountId().equals(
        OPAAuthenticationProvider.getAccountIdForLoggedInUser())) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
      error.setErrorMessage(String.format(
          ErrorConstants.UNAUTHORIZED_VIEW_PROFILE, userAccount.getUserName()));
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }

  protected boolean validateChangeOfEmail(UserAccountValueObject userAccount,
      ValidationResult validationResult, String newEmail) {
    String email = userAccount.getEmailAddress();
    if (email == null || newEmail == null) {
      return true;
    }
    UserAccountValueObject existingUserAccount = getUserAccountDao()
        .selectByEmail(newEmail);
    if (existingUserAccount != null
        && !userAccount.getUserName().trim()
            .equals(existingUserAccount.getUserName().trim())) {
      ValidationError error = new ValidationError();
      error.setErrorCode(OpaErrorCodeConstants.EMAIL_EXISTS);
      error.setErrorMessage(String.format(
          ErrorConstants.USER_EMAIL_ALREADY_EXISTS, newEmail));
      validationResult.addCustomValidationError(error);
      return false;
    }

    return true;
  }

  protected boolean validateNaraEmail(UserAccountValueObject userAccount,
      ValidationResult validationResult, String newEmail) {
    if (StringUtils.isNullOrEmtpy(newEmail)
        || userAccount.getEmailAddress().endsWith(configService.getConfig().getNaraEmail())) {
      return true;
    } else if (newEmail.endsWith(configService.getConfig().getNaraEmail())) {
      ValidationError error = new ValidationError();
      error.setErrorCode(OpaErrorCodeConstants.INVALID_EMAIL);
      error.setErrorMessage(String.format(
          ErrorConstants.USER_EMAIL_NOT_ALLOWED, newEmail));
      validationResult.addCustomValidationError(error);

      return false;
    }
    return true;
  }
}
