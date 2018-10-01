package gov.nara.opa.api.validation.search;

import gov.nara.opa.api.search.SearchErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SearchValidator {

  private Boolean isValid;
  private SearchErrorCode errorCode;

  public void validate(String action, String format) {
    isValid = true;
    if (!validateAction(action))
      return;
    if (!validateFormat(format))
      return;
  }

  /**
   * action parameter check (cannot be empty)
   * 
   * @param action
   *          action
   * @return isValid (true/false)
   */
  public boolean validateAction(String action) {
    if (action == null
        || action.isEmpty()
        || (!action.equalsIgnoreCase("search")
            && !action.equalsIgnoreCase("newList")
            && !action.equalsIgnoreCase("addList") && !action
              .equalsIgnoreCase("export"))) {
      isValid = false;
      errorCode = SearchErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Invalid action value");
    }
    return isValid;
  }

  /**
   * Format parameter check (can equal: NULL, xml or json)
   * 
   * @param format
   *          response format
   * @return isValid (true/false)
   */
  public boolean validateFormat(String format) {
    if (!format.equals("xml") && !format.equals("json")) {
      isValid = false;
      errorCode = SearchErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Invalid format value");
    }
    return isValid;
  }

  public Boolean getIsValid() {
    return isValid;
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  public SearchErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(SearchErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public void setStandardError(SearchErrorCode errorCode) {
    setIsValid(false);
    setErrorCode(errorCode);
    switch (errorCode) {
      case INVALID_API_CALL:
        getErrorCode().setErrorMessage("The API call type is not valid");
        break;
      case NOT_API_LOGGED_IN:
        getErrorCode().setErrorMessage(
            "You must be logged in to perform this action");
        break;
      case ROWS_LIMIT_EXCEEDED:
        getErrorCode().setErrorMessage("The rows limit has been exceeded for this user type");
        break;

      case INVALID_OFFSET_LIMIT:
        getErrorCode().setErrorMessage("The offset limit is not valid");
        break;
      case SYSTEM_ERROR:
        getErrorCode()
            .setErrorMessage(
                "The search did not run successfully - searchService-search returned NULL");
        break;
      default:
        break;
    }
  }

}
