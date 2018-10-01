package gov.nara.opa.api.validation.ingestion;

import gov.nara.opa.api.ingestion.UpdatePageNumErrorCode;
import gov.nara.opa.api.services.search.OpaSearchService;
import gov.nara.opa.common.NumericConstants;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class Created to manage the validation of parameters recieved to handle the
 * List catalog
 */
@Component
public class UpdatePageNumValidator {

  @Autowired
  private OpaSearchService opaSearchService;

  private UpdatePageNumErrorCode errorCode;
  private Boolean isValid;
  private String message;

  public UpdatePageNumErrorCode getErrorCode() {
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

  public void setErrorCode(UpdatePageNumErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  public void setMessage(String message) {
    this.message = message;
    getErrorCode().setErrorMessage(message);
  }

  public void setStandardError(UpdatePageNumErrorCode errorCode) {
    setIsValid(false);
    setErrorCode(errorCode);
    switch (errorCode) {
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
      errorCode = UpdatePageNumErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'format' value");
    }
    return isValid;
  }

  /**
   * Validate naId parameter check (cannot be empty or greater than 50
   * characters).
   * 
   * @param naId
   *          listName value to be validated
   * @return isValid (true/false)
   */
  private boolean validateNaId(String naId) {

    if (naId == null || naId.isEmpty()) {
      isValid = false;
      errorCode = UpdatePageNumErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + "Please specify a valid naId");
		} else if (naId.length() > NumericConstants.NA_ID_LENGTH) {
      isValid = false;
      errorCode = UpdatePageNumErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + getFieldSizeExceededMessage("naId", naId.length(), NumericConstants.NA_ID_LENGTH));
    }
    return isValid;
  }

  /**
   * Validate objectId parameter check (cannot be empty or greater than 50
   * characters).
   * 
   * @param objectId
   *          objectId value to be validated
   * @return isValid (true/false)
   */
  private boolean validateObjectId(String objectId) {

    if (objectId == null || objectId.isEmpty()) {
      isValid = false;
      errorCode = UpdatePageNumErrorCode.INVALID_PARAMETER;
      setMessage(getMessage() + (message.length() > 0 ? ". " : "")
          + "Please specify a valid objectId");
    } else if (objectId.length() > NumericConstants.OBJECT_ID_LENGTH) {
      isValid = false;
      errorCode = UpdatePageNumErrorCode.INVALID_PARAMETER;
      setMessage(getMessage()
          + (message.length() > 0 ? ". " : "")
          + getFieldSizeExceededMessage("objectId", objectId.length(),
              NumericConstants.OBJECT_ID_LENGTH));
    }
    return isValid;
  }

  /**
   * Validate pageNum parameter (has to be greater that -1)
   * 
   * @param pageNum
   *          pageNum parameter greater that -1
   * @return isValid (true/false)
   */
  private boolean validatePageNum(int pageNum) {

    if (pageNum < -1) {
      isValid = false;
      errorCode = UpdatePageNumErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'pageNum' value");
    }
    return isValid;
  }

  /**
   * Validate the parameters received on the request
   * 
   * @param naId
   *          naId value to be validated
   * @param objectId
   *          objectId value to be validated
   * @param pageNum
   *          pageNum value to be validated
   * @param format
   *          format value to be validated
   * @param pretty
   *          pretty value to be validated
   */
  public void validateParameters(HashMap<String, Object> parameters) {

    resetValidation();

    if (parameters.containsKey("naId"))
      if (!validateNaId((String) parameters.get("naId")))
          return;

    if (parameters.containsKey("objectId"))
      if (!validateObjectId((String) parameters.get("objectId")))
          return;

    if (parameters.containsKey("format"))
      if (!validateFormat((String) parameters.get("format")))
          return;

    if (parameters.containsKey("pageNum"))
      if (!validatePageNum((int) parameters.get("pageNum")))
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
      errorCode = UpdatePageNumErrorCode.INVALID_PARAMETER;
      setMessage("Invalid parameter 'pretty' value");
    }
    return isValid;
  }

}
