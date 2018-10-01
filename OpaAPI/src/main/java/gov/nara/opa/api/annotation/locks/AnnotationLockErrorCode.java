package gov.nara.opa.api.annotation.locks;

import gov.nara.opa.api.services.ErrorCode;

public enum AnnotationLockErrorCode implements ErrorCode {
  NONE, NOT_FOUND, INTERNAL_ERROR, INVALID_PARAMETER, INVALID_API_CALL, NOT_API_LOGGED_IN, LOCK_LIMIT_REACHED, LOCKED_BY_ANOTHER, NO_LOCK;

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
