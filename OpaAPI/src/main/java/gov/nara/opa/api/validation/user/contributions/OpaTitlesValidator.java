package gov.nara.opa.api.validation.user.contributions;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.user.contributions.UserContributionsErrorCode;
import gov.nara.opa.common.NumericConstants;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class Created to manage the validation of parameters recieved to handle the
 * view tagged titles method
 */
@Component
public class OpaTitlesValidator {
  
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
   * @param tagText
   *          tagText value to be validated
   * @param format
   *          format value to be validated
   * @param pretty
   *          pretty value to be validated
   */
  public void validateParameters(HashMap<String, Object> parameters) {

    resetValidation();
    if (parameters.containsKey("tagtext"))
      if (!validateTagText((String) parameters.get("tagtext")))
        return;
    if (parameters.containsKey("username"))
      if (!validateUserName((String) parameters.get("username")))
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
   * List name parameter check (cannot be empty or greater than 50 characters).
   * 
   * @param listName
   *          listName value to be validated
   * @return isValid (true/false)
   */
  private boolean validateTagText(String tagText) {
    if (tagText == null || tagText.isEmpty()) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + "Please enter a tag text");
    } else if (tagText.length() > NumericConstants.TAG_TEXT_LENGTH) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage(getMessage()
          + (message.length() > 0 ? ". " : "")
          + getFieldSizeExceededMessage("tagText", tagText.length(),
							NumericConstants.TAG_TEXT_LENGTH));
    }
    return isValid;
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
  private boolean validateRows(int rows) {
    if (rows < 0) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'rows' value");
    }
    if (rows > configService.getConfig().getMaxOpaTitlesRows()) {
      isValid = false;
      errorCode = UserContributionsErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + " The maximum value for 'rows' is " + configService.getConfig().getMaxOpaTitlesRows());
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
      case TITLES_NOT_FOUND:
        getErrorCode().setErrorMessage("No titles found");
        break;
      default:
        break;
    }
  }
}
