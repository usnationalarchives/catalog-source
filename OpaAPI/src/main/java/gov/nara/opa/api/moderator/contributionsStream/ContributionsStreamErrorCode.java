package gov.nara.opa.api.moderator.contributionsStream;

import gov.nara.opa.api.services.ErrorCode;

public enum ContributionsStreamErrorCode implements ErrorCode {
  NONE, NOT_FOUND, INTERNAL_ERROR, MISSING_PARAM, INVALID_PARAM, USER_NOT_FOUND, INSUFFICIENT_PRIVILEGES, NOT_API_LOGGED_IN;

  private String errorMessage;

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

}
