package gov.nara.opa.api.annotation.transcriptions;

import gov.nara.opa.api.services.ErrorCode;

public enum TranscriptionErrorCode implements ErrorCode {
  NONE, NOT_FOUND, INTERNAL_ERROR, INVALID_PARAMETER, INVALID_API_CALL, NOT_API_LOGGED_IN, EMBEDDED_HTML, NO_LOCK, LOCKED_BY_ANOTHER, ILLEGAL_CHARACTERS, NOT_AUTHORIZED, DUPLICATE_ANNOTATION, CREATE_TRANSCRIPTION_FAILED;

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
