package gov.nara.opa.api.user.accounts;

import gov.nara.opa.api.services.ErrorCode;

public enum UserAccountErrorCode implements ErrorCode {
  NONE, INVALID_API_CALL, // invalid API call type (must be equal to: opa OR
                          // api)
  INVALID_LOGIN, // invalid login request
  NOT_API_LOGGED_IN, // session validation error (token)
  NOT_OWNER, // logged user does not own the data to be modified
  BAD_PASSWORD, // invalid password
  USER_NOT_FOUND, // self explanatory
  MISSING_PASSWORD, // password not provided
  MISSING_PARAM, // parameter not provided
  USER_EXISTS, // self
  EMAIL_EXISTS, // self
  INVALID_PARAMETER, // invalid parameter other than password
  INTERNAL_ERROR, // self
  INVALID_ACCOUNT_DATA, // self
  ACCOUNT_INACTIVE, // self
  MISSING_REASON, // self
  MISSING_NOTES, // self
  ACCOUNT_LOCKED; // self

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

}
