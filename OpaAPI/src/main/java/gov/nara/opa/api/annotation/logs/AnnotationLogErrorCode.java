package gov.nara.opa.api.annotation.logs;

import gov.nara.opa.api.services.ErrorCode;

public enum AnnotationLogErrorCode implements ErrorCode {
  NONE, NOT_FOUND, INTERNAL_ERROR;

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
