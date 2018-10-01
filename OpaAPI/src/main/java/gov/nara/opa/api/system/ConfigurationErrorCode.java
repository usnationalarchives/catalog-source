package gov.nara.opa.api.system;

import gov.nara.opa.api.services.ErrorCode;

public enum ConfigurationErrorCode implements ErrorCode {

  NONE, NOT_API_LOGGED_IN, INVALID_API_CALL, INVALID_PARAMETER, CONFIG_FILE_NOT_FOUND;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

}
