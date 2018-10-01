package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VerifyAccountValidator extends CommonUserAccountValidator {

  @Value("${registerVerificationMaximumDays}")
  private int registerVerificationMaximumDays;

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    validateActivationCode(validationResult);

  }

  protected void validateActivationCode(ValidationResult validationResult) {
    String activationCode = ((VerifyAccountRequestParameters) validationResult
        .getValidatedRequest()).getActivationCode();
    UserAccountValueObject userAccount = getUserAccountDao()
        .getUserAccountByActivationCode(activationCode);
    if (userAccount == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
      error.setErrorMessage(ErrorConstants.INVALID_USER_ACTIVATION_CODE);
      validationResult.addCustomValidationError(error);
      return;
    }
    if (!validateActivationCodeTimeWindow(validationResult, userAccount)) {
      return;
    }
    validationResult.addContextObject(USER_ACCOUNT_OBJECT_KEY, userAccount);
  }

  protected boolean validateActivationCodeTimeWindow(
      ValidationResult validationResult, UserAccountValueObject userAccount) {
    if (!isWithinVerificationTimeWindow(userAccount)) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
      error.setErrorMessage(ErrorConstants.EXPIRED_ACTIVATION_CODE_WINDOW);
      validationResult.addCustomValidationError(error);
      return false;
    }

    return true;
  }

  /**
   * Determines if the current date is contained between the verification
   * timestamp and the provided days and hours.
   * 
   * @param user
   *          The user account to verify
   * @return True if the current date is within the time window
   */
  protected boolean isWithinVerificationTimeWindow(UserAccountValueObject user) {
    return user.getVerificationTS().after(
        DateTime.now().minusDays(registerVerificationMaximumDays).toDate());
  }

}
