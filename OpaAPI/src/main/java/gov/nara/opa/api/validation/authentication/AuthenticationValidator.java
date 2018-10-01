package gov.nara.opa.api.validation.authentication;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccountErrorCode;

import org.springframework.stereotype.Component;

@Component
public class AuthenticationValidator {

  private Boolean isValid;
  private UserAccountErrorCode errorCode;

  public AuthenticationValidator() {
    resetValidation();
  }
  
  public void resetValidation() {
    isValid = true;
    errorCode = UserAccountErrorCode.NONE;
  }
  
  /**
   * Method to validate the login API parameters
   * 
   * @param userName
   *          login user name
   * @param pwd
   *          login password
   * @param format
   *          OPA Response Object Format
   */
  public void validateLogin(String userName, String pwd, String format) {
    isValid = true;
    if (!validateUserName(userName))
      return;
    if (!validatePwd(pwd))
      return;
    if (!validateFormat(format))
      return;
  }

  /**
   * Method to validate the login API parameters
   * 
   * @param format
   *          OPA Response Object Format
   */
  public void validateLogin(String format) {
    isValid = true;
    if (!validateFormat(format))
      return;
  }

  /**
   * User name parameter check (cannot be empty)
   * 
   * @param userName
   *          Username to login with
   * @return isValid (true/false)
   */
  public boolean validateUserName(String userName) {
    if (userName == null || userName.isEmpty()) {
      isValid = false;
      errorCode = UserAccountErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Please fill in all fields to login.");
    }
    return isValid;
  }

  /**
   * Password parameter check (cannot be empty)
   * 
   * @param pwd
   *          Password to login with
   * @return isValid (true/false)
   */
  public boolean validatePwd(String pwd) {
    if (pwd == null || pwd.isEmpty()) {
      isValid = false;
      errorCode = UserAccountErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Please fill in all fields to login.");
    }
    return isValid;
  }

  /**
   * Format parameter check (can equal: NULL, xml or json)
   * 
   * @param format
   *          response format
   * @return isValid (true/false)
   */
  public boolean validateFormat(String format) {
    if (!format.equals("xml") && !format.equals("json")) {
      isValid = false;
      errorCode = UserAccountErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Invalid format value");
    }
    return isValid;
  }

  public Boolean getIsValid() {
    return isValid;
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  public UserAccountErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(UserAccountErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public void setStandardError(UserAccountErrorCode errorCode) {
    setIsValid(false);
    setErrorCode(errorCode);
    switch (errorCode) {
      case INVALID_API_CALL:
        getErrorCode().setErrorMessage("The API call type is not valid");
        break;
      case INVALID_LOGIN:
        getErrorCode().setErrorMessage("Invalid Credentials");
        break;
      case NOT_API_LOGGED_IN:
        getErrorCode().setErrorMessage(
            "You must be logged in to perform this action");
        break;
      case INVALID_PARAMETER:
        getErrorCode().setErrorMessage(ErrorConstants.invalidParameter);
        break;
      default:
        break;
    }
  }

}
