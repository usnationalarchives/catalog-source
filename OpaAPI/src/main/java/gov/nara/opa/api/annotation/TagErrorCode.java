package gov.nara.opa.api.annotation;

import gov.nara.opa.api.services.ErrorCode;

public enum TagErrorCode implements ErrorCode {

  NONE, NOT_API_LOGGED_IN, INVALID_API_CALL, DUPLICATE_ANNOTATION, INVALID_PARAMETER, EMBEDDED_HTML, INVALID_CHARACTERS, CONFIG_FILE_NOT_FOUND, TAG_SIZE_EXCEEDED, CREATE_TAG_FAILED, DELETE_TAG_FAILED, TAG_NOT_FOUND, NO_TAGS_FOUND, NOT_OWNER, INTERNAL_ERROR, USERNAME_NOT_FOUND;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
