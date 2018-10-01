package gov.nara.opa.api.validation.system;

import gov.nara.opa.api.system.ConfigurationErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationValidator {

  private Boolean isValid;
  private ConfigurationErrorCode errorCode;

  /**
   * Method to validate the get configuration API parameters
   * 
   * @param format
   *          OPA Response Object Format
   */
  public void validate(String format) {
    isValid = true;

    if (!validateFormat(format))
      return;
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
      errorCode = ConfigurationErrorCode.INVALID_PARAMETER;
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

  public ConfigurationErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(ConfigurationErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public void setStandardError(ConfigurationErrorCode errorCode) {
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
      case CONFIG_FILE_NOT_FOUND:
        getErrorCode().setErrorMessage("The Configuration file was not found");
        break;
      default:
        break;
    }
  }

}
