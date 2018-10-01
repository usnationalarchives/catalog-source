package gov.nara.opa.api.validation.moderator;

import gov.nara.opa.api.moderator.ModeratorErrorCode;
import gov.nara.opa.api.validation.ValidatorBase;
import gov.nara.opa.common.NumericConstants;

import java.util.HashMap;

import org.springframework.stereotype.Component;

/**
 * Class Created to manage the validation of parameters recieved to handle the
 * List catalog
 */
@Component
public class ModeratorValidator extends ValidatorBase<ModeratorErrorCode> {

  //private int textFieldLength = 50;

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

  @Override
  public void resetValidation() {
    super.resetValidation();
    errorCode = ModeratorErrorCode.NONE;
  }

  /**
   * Will set the response error code and description depending on the error
   * cause.
   * 
   * @param errorCode
   *          ModeratorErrorCode that will depend on the error circunstances
   */
  public void setStandardError(ModeratorErrorCode errorCode) {
    setIsValid(false);
    setErrorCode(errorCode);
    switch (errorCode) {
      case MISSING_PARAM:
        getErrorCode().setErrorMessage("A parameter is missing");
        break;
      case TEXT_TO_LONG:
        getErrorCode().setErrorMessage("The text parameter is too long");
        break;
      case INVALID_VALUE:
        getErrorCode().setErrorMessage(
            "The value specified for a parameter is incorrect");
        break;
      case NOT_API_LOGGED_IN:
        getErrorCode().setErrorMessage(
            "You must be logged in to perform this action");
        break;
      case INSUFFICIENT_PRIVILEGES:
        getErrorCode().setErrorMessage(
            "Moderator privileges are required to execute this operation");
        break;
      case CREATE_REASON_FAILED:
        getErrorCode()
            .setErrorMessage(
                "The reason was not created - reasonService-createReason returned NULL");
        break;
      case REASONS_NOT_FOUND:
        getErrorCode().setErrorMessage("No annotation reasons found");
        break;
      case DUPLICATE_REASON:
        getErrorCode().setErrorMessage(
            "A reason you are trying to add already exists");
        break;
      default:
        break;
    }
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
      errorCode = ModeratorErrorCode.INVALID_VALUE;
      setMessage("Format value is not valid");
    }
    return isValid;
  }

  /**
   * Validate the parameters received on the request
   * 
   * @param parameters
   *          Map with the parameters that we will validate.
   */
  public void validateParameters(HashMap<String, Object> parameters) {

    resetValidation();

    if (parameters.containsKey("text"))
      if (!validateText((String) parameters.get("text")))
        return;

    if (parameters.containsKey("format"))
      if (!validateFormat((String) parameters.get("format")))
        return;

    if (parameters.containsKey("pretty"))
      if (!validatePretty((boolean) parameters.get("pretty")))
        return;
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
      errorCode = ModeratorErrorCode.INVALID_VALUE;
      setMessage("Pretty value is not valid");
    }
    return isValid;
  }

  /**
   * Text parameter check (cannot be empty or greater than 50 characters).
   * 
   * @param text
   *          text value to be validated
   * @return isValid (true/false)
   */
  private boolean validateText(String text) {

    if (text == null || text.isEmpty()) {
      isValid = false;
      errorCode = ModeratorErrorCode.MISSING_PARAM;
      setMessage("Reason text cannot be empty");
		} else if (text.length() > NumericConstants.REASON_TEXT_LENGTH) {
      isValid = false;
      errorCode = ModeratorErrorCode.TEXT_TO_LONG;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + getFieldSizeExceededMessage("text", text.length(), NumericConstants.REASON_TEXT_LENGTH));
    }
    return isValid;
  }

}
