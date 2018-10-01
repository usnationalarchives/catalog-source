package gov.nara.opa.api.user.contributions;

import gov.nara.opa.api.services.ErrorCode;

/**
 * Enum file used to manage the possible error that the List catalog may return.
 */
public enum UserContributionsErrorCode implements ErrorCode {
  NONE, TITLES_NOT_FOUND, NOT_API_LOGGED_IN, USER_NOT_FOUND, NOT_OWNER, INTERNAL_ERROR, INVALID_PARAMETER, ACCOUNT_INACTIVE, CONTRIBUTIONS_NOT_FOUND;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}