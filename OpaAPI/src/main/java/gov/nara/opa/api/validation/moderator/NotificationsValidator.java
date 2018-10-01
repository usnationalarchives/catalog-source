package gov.nara.opa.api.validation.moderator;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ValidatorBase;
import gov.nara.opa.api.valueobject.user.notifications.UserNotificationErrorCode;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class Created to manage the validation of parameters recieved to handle the
 * List catalog
 */
@Component
public class NotificationsValidator extends
    ValidatorBase<UserNotificationErrorCode> {

  @Autowired
  private ConfigurationService configService;
  
  @Override
  public void resetValidation() {
    super.resetValidation();
    errorCode = UserNotificationErrorCode.NONE;
  }

  /**
   * Will set the response error code and description depending on the error
   * cause.
   * 
   * @param errorCode
   *          ModeratorErrorCode that will depend on the error circunstances
   */
  public void setStandardError(UserNotificationErrorCode errorCode) {
    setIsValid(false);
    setErrorCode(errorCode);
    switch (errorCode) {
      case NOT_API_LOGGED_IN:
        getErrorCode().setErrorMessage(
            "You must be logged in to perform this action");
        break;
      case NOTIFICATIONS_NOT_FOUND:
        getErrorCode().setErrorMessage("Notifications not found");
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
      errorCode = UserNotificationErrorCode.INVALID_VALUE;
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

    if (parameters.containsKey("format"))
      if (parameters.containsKey("format"))
        if (!validateFormat((String) parameters.get("format")))
          return;

    if (parameters.containsKey("pretty"))
      if (parameters.containsKey("pretty"))
        if (!validatePretty((boolean) parameters.get("pretty")))
          return;

    if (parameters.containsKey("offset"))
      if (parameters.containsKey("offset"))
        if (!validateOffset((int) parameters.get("offset")))
          return;

    if (parameters.containsKey("rows"))
      if (parameters.containsKey("rows"))
        if (!validateRows((int) parameters.get("rows")))
          return;

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
      errorCode = UserNotificationErrorCode.INVALID_PARAMETER;
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
      errorCode = UserNotificationErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'rows' value");
    }
    if (rows > configService.getConfig().getMaxNotificationRows()) {
      isValid = false;
      errorCode = UserNotificationErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + String.format(ErrorConstants.MAX_ROWS_ALLOWED, configService.getConfig().getMaxNotificationRows()));
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
      errorCode = UserNotificationErrorCode.INVALID_VALUE;
      setMessage("Pretty value is not valid");
    }
    return isValid;
  }

}
