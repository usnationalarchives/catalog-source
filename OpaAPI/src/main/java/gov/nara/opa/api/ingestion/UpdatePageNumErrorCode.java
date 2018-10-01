package gov.nara.opa.api.ingestion;

import gov.nara.opa.api.services.ErrorCode;

/**
 * Enum file used to manage the possible error that the updatePageNum catalog
 * may return.
 */
public enum UpdatePageNumErrorCode implements ErrorCode {
  NONE, INTERNAL_ERROR, INVALID_PARAMETER;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}