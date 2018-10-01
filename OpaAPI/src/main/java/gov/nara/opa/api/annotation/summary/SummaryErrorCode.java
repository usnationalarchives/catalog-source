package gov.nara.opa.api.annotation.summary;

import gov.nara.opa.api.services.ErrorCode;

/**
 * Enum file used to manage the possible error that the annotation_reason
 * catalog may return.
 */
public enum SummaryErrorCode implements ErrorCode {
  NONE, MISSING_PARAM, TEXT_TO_LONG, INVALID_VALUE, INVALID_PARAMETER, INTERNAL_ERROR, USER_NOT_FOUND, INSUFFICIENT_PRIVILEGES, NOT_API_LOGGED_IN, CONTRIBUTIONS_NOT_FOUND;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}