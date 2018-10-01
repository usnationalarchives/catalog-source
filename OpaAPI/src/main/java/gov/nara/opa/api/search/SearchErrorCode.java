package gov.nara.opa.api.search;

import gov.nara.opa.api.services.ErrorCode;

public enum SearchErrorCode implements ErrorCode {

  NONE, NOT_API_LOGGED_IN, INVALID_API_CALL, SYSTEM_ERROR, INVALID_PARAMETER, EMBEDDED_HTML, INVALID_CHARACTERS, ROWS_LIMIT_EXCEEDED, INVALID_OFFSET_LIMIT, QUERY_TIMEOUT;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
