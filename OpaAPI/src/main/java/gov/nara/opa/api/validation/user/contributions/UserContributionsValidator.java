package gov.nara.opa.api.validation.user.contributions;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.user.contributions.UserContributionsErrorCode;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class Created to manage the validation of parameters recieved to handle the
 * Contributions summary catalog
 */
@Component
public class UserContributionsValidator {

  @Autowired
  private ConfigurationService configService;
  
  private UserContributionsErrorCode errorCode;
  private Boolean isValid;
  private String message;

  public UserContributionsErrorCode getErrorCode() {
    return errorCode;
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

  public void setErrorCode(UserContributionsErrorCode errorCode) {
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
    validateParameters(parameters, true);
  }
    
    
  public void validateParameters(HashMap<String, Object> parameters, boolean validateMaxRows) {

    resetValidation();

    if (parameters.containsKey("username"))
      if (!validateUserName((String) parameters.get("username")))
        return;

    if (parameters.containsKey("fullStats"))
      if (!validateFullStats((boolean) parameters.get("fullStats")))
        return;

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
      if (!validateRows((int) parameters.get("rows"), validateMaxRows))
        return;

    if (parameters.containsKey("sort"))
      if (!validateSort((String) parameters.get("sort")))
        return;
  }

  /**
   * User name parameter check
   * 
   * @param listName
   *          listName value to be validated
   * @return isValid (true/false)
   */
  private boolean validateUserName(String userName) {
    if (userName == null || userName.isEmpty()) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + "Please enter a username");
    }
    return isValid;
  }

  /**
   * sort parameter check (can equal: tag DESC, tag ASC, count DESC, count ASC)
   * 
   * @param sort
   *          Contributed tags sort config
   * @return isValid (true/false)
   */
  private boolean validateSort(String sort) {
    if (!sort.equals("tag DESC") && !sort.equals("tag ASC")
        && !sort.equals("count DESC") && !sort.equals("count ASC")) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'sort' value. (Supported values: tag DESC, tag ASC, count DESC, count ASC) ");
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
  private boolean validateFormat(String format) {
    if (!format.equals("xml") && !format.equals("json")) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'format' value");
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
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'pretty' value");
    }
    return isValid;
  }

  /**
   * fullStats parameter check (can equal: true or false)
   * 
   * @param fullStats
   *          fullStats detailed or brief stats
   * @return isValid (true/false)
   */
  private boolean validateFullStats(boolean fullStats) {
    if (fullStats != true && fullStats != false) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'fullStats' value");
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
  private boolean validateOffset(int offset) {
    if (offset < 0) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
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
  private boolean validateRows(int rows, boolean validateMaxRows) {
    if (rows < 0) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'rows' value");
    }
    if (validateMaxRows && rows > configService.getConfig().getMaxContributionRows()) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + " The maximum value for 'rows' is " + configService.getConfig().getMaxContributionRows());
    }
    return isValid;
  }

  public void setStandardError(UserContributionsErrorCode errorCode) {
    setIsValid(false);
    setErrorCode(errorCode);
    switch (errorCode) {
      case USER_NOT_FOUND:
        getErrorCode().setErrorMessage("The username does not exist");
        break;
      case INTERNAL_ERROR:
        getErrorCode().setErrorMessage("Unspecified error occurred");
        break;
      case ACCOUNT_INACTIVE:
        getErrorCode().setErrorMessage("Invalid user account status");
        break;
      case NOT_OWNER:
        getErrorCode().setErrorMessage("Permission Denied");
        break;
      case CONTRIBUTIONS_NOT_FOUND:
        getErrorCode().setErrorMessage("No contributions found");
        break;
      case TITLES_NOT_FOUND:
        getErrorCode().setErrorMessage("No titles found");
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
