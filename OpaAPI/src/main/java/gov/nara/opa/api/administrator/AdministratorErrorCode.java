package gov.nara.opa.api.administrator;

import gov.nara.opa.api.services.ErrorCode;

/**
 * Enum file used to manage the possible error that the annotation_reason
 * catalog may return.
 */
public enum AdministratorErrorCode implements ErrorCode {
  NONE, MISSING_PARAM, TEXT_TO_LONG, INVALID_VALUE, INTERNAL_ERROR, USER_NOT_FOUND, INSUFFICIENT_PRIVILEGES, NOT_API_LOGGED_IN, REASONS_NOT_FOUND, DUPLICATE_REASON, CREATE_REASON_FAILED, INVALID_PARAMETER;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}