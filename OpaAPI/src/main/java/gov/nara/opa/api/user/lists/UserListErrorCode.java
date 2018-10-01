package gov.nara.opa.api.user.lists;

import gov.nara.opa.api.services.ErrorCode;

/**
 * Enum file used to manage the possible error that the List catalog may return.
 */
public enum UserListErrorCode implements ErrorCode {
  NONE, INVALID_LIST_ITEM, USER_NOT_FOUND, NOT_OWNER, DUPLICATE_LIST, DUPLICATE_LIST_ITEM, INVALID_LIST, INTERNAL_ERROR, INVALID_PARAMETER, ACCOUNT_INACTIVE, ENTRIES_NOT_FOUND, EMPTY_LIST, LISTS_NOT_FOUND, MAX_LIST_NUMBER_REACHED, MAX_ITEM_NUMBER_REACHED, NOT_API_LOGGED_IN;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}