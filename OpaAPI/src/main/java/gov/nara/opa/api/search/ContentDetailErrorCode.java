package gov.nara.opa.api.search;

public enum ContentDetailErrorCode {

  NONE, NOT_API_LOGGED_IN, INVALID_API_CALL, SYSTEM_ERROR, INVALID_PARAMETER, FILE_NOT_FOUND, EMBEDDED_HTML, INVALID_CHARACTERS;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
