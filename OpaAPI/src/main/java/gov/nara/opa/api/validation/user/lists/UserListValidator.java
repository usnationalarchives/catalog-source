package gov.nara.opa.api.validation.user.lists;

import gov.nara.opa.api.services.search.OpaSearchService;
import gov.nara.opa.api.user.lists.UserListErrorCode;
import gov.nara.opa.common.NumericConstants;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class Created to manage the validation of parameters recieved to handle the
 * List catalog
 */
@Component
public class UserListValidator {

  @Autowired
  private OpaSearchService opaSearchService;

  private UserListErrorCode errorCode;
  private Boolean isValid;
  private String message;

  public UserListErrorCode getErrorCode() {
    return errorCode;
  }

  /**
   * Creates the message for database field size exceeded
   * 
   * @param fieldName
   *          The name of the entity field
   * @param currentLength
   *          The content size
   * @param limit
   *          The database field size
   * @return The formatted error message
   */
  private String getFieldSizeExceededMessage(String fieldName,
      int currentLength, int limit) {
    return String.format(
        "Text size of field '%1$s' (%2$d) is greater than field size: %3$d",
        fieldName, currentLength, limit);
  }

  public Boolean getIsValid() {
    return isValid;
  }

  public String getMessage() {
    return message;
  }

  public void resetValidation() {
    message = "";
    isValid = true;
  }

  public void setErrorCode(UserListErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  public void setMessage(String message) {
    this.message = message;
    getErrorCode().setErrorMessage(message);
  }

  /**
   * Validate the parameters received on the request
   * 
   * @param listName
   *          listname value to be validated
   * @param userName
   *          userName value to be validated
   * @param format
   *          format value to be validated
   * @param pretty
   *          pretty value to be validated
   */
  public void validateParameters(HashMap<String, Object> parameters) {

    resetValidation();

    if (parameters.containsKey("userName"))
      if (!validateUserName((String) parameters.get("userName")))
        return;

    if (parameters.containsKey("listname"))
      if (!validateListName((String) parameters.get("listname")))
        return;
    
    if (parameters.containsKey("listName"))
      if (!validateListName((String) parameters.get("listName")))
        return;
    

    if (parameters.containsKey("newName"))
      if (!validateNewListName((String) parameters.get("newName")))
        return;

    if (parameters.containsKey("newname"))
      if (!validateNewListName((String) parameters.get("newname")))
        return;
    
    
    if (parameters.get("action") != null
        && !parameters.get("action").equals("addToListFromSearch")) {
      if (parameters.containsKey("what"))
        if (!validateWhat((String) parameters.get("what")))
          return;
    }

    if (parameters.containsKey("format"))
      if (!validateFormat((String) parameters.get("format")))
        return;

    if (parameters.containsKey("pretty"))
      if (!validatePretty((boolean) parameters.get("pretty")))
        return;

    if (parameters.containsKey("offset"))
      if (!validateOffset((int) parameters.get("offset")))
        return;

    if (parameters.containsKey("rows"))
      if (!validateRows((int) parameters.get("rows")))
        return;
  }

  /**
   * Format parameter check (can equal: NULL, xml or json)
   * 
   * @param format
   *          response format
   * @return isValid (true/false)
   */
  private boolean validateFormat(String format) {
    if (!format.equals("xml") && !format.equals("json")) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'format' value");
    }
    return isValid;
  }

  /**
   * List name parameter check (cannot be empty or greater than 50 characters).
   * 
   * @param listName
   *          listName value to be validated
   * @return isValid (true/false)
   */
  private boolean validateListName(String listName) {

    if (listName == null || listName.isEmpty()) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + "Please enter a new list name");
		} else if (listName.length() > NumericConstants.LIST_NAME_LENGTH) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage(getMessage()
          + (message.length() > 0 ? ". " : "")
          + getFieldSizeExceededMessage("listName", listName.length(),
              NumericConstants.LIST_NAME_LENGTH));
    }
    return isValid;
  }

  /**
   * Set the Ducplicate List Name error message.
   * 
   * @param listName
   *          listName value to be validated
   * @return isValid (true/false)
   */
  public void setDuplicateListError(String listName) {
    setIsValid(false);
    setErrorCode(UserListErrorCode.DUPLICATE_LIST);
    getErrorCode().setErrorMessage(
        listName + " list name already exists. Please choose a different name");
  }

  /**
   * List name parameter check (cannot be empty or greater than 50 characters).
   * 
   * @param newListName
   *          listName value to be validated
   * @return isValid (true/false)
   */
  private boolean validateNewListName(String newListName) {

    if (newListName == null || newListName.isEmpty()) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + "New list name cannot be empty");
    } else if (newListName.length() > NumericConstants.LIST_NAME_LENGTH) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage(getMessage()
          + (message.length() > 0 ? ". " : "")
          + getFieldSizeExceededMessage("listName", newListName.length(),
              NumericConstants.LIST_NAME_LENGTH));
    }
    return isValid;
  }

  /**
   * Pretty parameter check (can equal: true or false)
   * 
   * @param pretty
   *          pretty-print (true/false)
   * @return isValid (true/false)
   */
  private boolean validatePretty(boolean pretty) {
    if (pretty != true && pretty != false) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'pretty' value");
    }
    return isValid;
  }

  /**
   * Offset parameter check (can equal: true or false)
   * 
   * @param Offset
   *          Offset parameter greater that zero
   * @return isValid (true/false)
   */
  private boolean validateOffset(int offset) {
    if (offset < 0) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'offset' value");
    }
    return isValid;
  }

  /**
   * Rows parameter check (can equal: true or false)
   * 
   * @param Rows
   *          Rows parameter greater that zero but less than 200
   * @return isValid (true/false)
   */
  private boolean validateRows(int rows) {

    int maxRowsAllowed = opaSearchService.getListLimitForUser();

    if (rows < 0) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'rows' value");
    }
    if (rows > maxRowsAllowed) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + " The maximum value for 'rows' is " + maxRowsAllowed);
    }
    return isValid;
  }

  /**
   * Parameter 'userName' check (cannot be empty or greater than 100
   * characters).
   * 
   * @param userName
   *          Username value to be validated
   * @return isValid (true/false)
   */
  private boolean validateUserName(String userName) {
    if (userName == null || userName.isEmpty() || userName.length() > 100) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'userName' value");
    }
    return isValid;
  }

  /**
   * Validate the what parameter
   * 
   * @param what
   *          Parameter what received on the request
   * @return isValid (true/false)
   */
  public boolean validateWhat(String what) {
    if (what == null || what.isEmpty()) {
      isValid = false;
      errorCode = UserListErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Parameter what cannot be empty");
    } else {
      String[] opaIds = what.split(",");
      for (String opaId : opaIds) {
        if (opaId.length() > NumericConstants.OPA_ID_LENGTH_FOR_LISTS && isValid == true) {
          isValid = false;
          errorCode = UserListErrorCode.INVALID_PARAMETER;
          setMessage(getMessage()
              + (message.length() > 0 ? ". " : "")
              + getFieldSizeExceededMessage("opaId", opaId.length(),
                  NumericConstants.OPA_ID_LENGTH_FOR_LISTS));
        }
      }
    }
    return isValid;
  }

  public void setStandardError(UserListErrorCode errorCode) {
    setIsValid(false);
    setErrorCode(errorCode);
    switch (errorCode) {
      case USER_NOT_FOUND:
        getErrorCode().setErrorMessage("The username does not exist");
        break;
      case ACCOUNT_INACTIVE:
        getErrorCode().setErrorMessage("Invalid user account status");
        break;
      case NOT_OWNER:
        getErrorCode().setErrorMessage("Permission Denied");
        break;
      case DUPLICATE_LIST:
        getErrorCode().setErrorMessage(
            "List name already exists. Please choose a different name");
        break;
      case INVALID_LIST:
        getErrorCode().setErrorMessage("The target list does not exist");
        break;
      case LISTS_NOT_FOUND:
        getErrorCode().setErrorMessage("No lists exist for this user account");
        break;
      case DUPLICATE_LIST_ITEM:
        getErrorCode().setErrorMessage("The list already contains the item(s)");
        break;
      case MAX_ITEM_NUMBER_REACHED:
        getErrorCode()
            .setErrorMessage(
                "The selected item(s) cannot be added - the maximum list size will be exceeded");
        break;
      case INVALID_LIST_ITEM:
        getErrorCode().setErrorMessage("The item does not exist");
        break;
      case ENTRIES_NOT_FOUND:
        getErrorCode().setErrorMessage(
            "The specified list entries were not found");
        break;
      case EMPTY_LIST:
        getErrorCode().setErrorMessage("The specified list is empty");
        break;
      case NOT_API_LOGGED_IN:
        getErrorCode().setErrorMessage(
            "You must be logged in to perform this action");
        break;
      default:
        break;
    }
  }
}
