package gov.nara.opa.api.validation.annotation.tags;

import gov.nara.opa.api.annotation.TagErrorCode;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Config;
import gov.nara.opa.common.NumericConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TagValidator {
  
  @Value("${configFilePath}")
  private String configFilePath;

  @Autowired
  private ConfigurationService configService;

  private Boolean isValid;
  private TagErrorCode errorCode;

  /**
   * Method to validate the tag API parameters
   * 
   * @param naId
   *          NARA ID
   * @param format
   *          OPA Response Object Format
   */

  public void validate(String naId, String objectId, String format) {
    isValid = true;

    if (!validateNaid(naId))
      return;
    if (!validateObjectId(objectId))
      return;
    if (!validateFormat(format))
      return;
  }

  public void validate(String naId, String objectId, String format,
      int annotationId) {
    isValid = true;

    validate(naId, objectId, format);
    if (isValid) {
      if (!validateAnnotationId(annotationId))
        return;
    }
  }

  /**
   * Method to validate the tag API parameters
   * 
   * @param naId
   *          NARA ID
   * @param tagText
   *          Tag annotation text value
   * @param format
   *          OPA Response Object Format
   */

  public void validate(String naId, String objectId, String tagText,
      String format) {
    isValid = true;

    validate(naId, objectId, format);
    if (isValid) {
      if (!validateTagText(tagText))
        return;
    }
  }

  public void validate(String naId, String objectId, String tagText,
      String format, int reasonId, String notes) {
    isValid = true;

    validate(naId, objectId, tagText, format);

    if (isValid) {
      if (!validateReasonId(reasonId))
        return;
      if (!validateNotes(notes))
        return;
    }
  }

  public boolean validateAnnotationId(int annotationId) {
    return validatePositiveInteger(annotationId, "Invalid annotation Id value");
  }

  public boolean validateNotes(String notes) {
		if (notes == null || notes.isEmpty()
				|| notes.length() > NumericConstants.NOTES_LENGTH) {
      isValid = false;
      errorCode = TagErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Invalid notes value");
    }
    return isValid;
  }

  public boolean validateReasonId(int reasonId) {
    return validatePositiveInteger(reasonId, "Invalid reason value");
  }

  public boolean validatePositiveInteger(int number, String errorMessage) {
    if (number < 0) {
      isValid = false;
      errorCode = TagErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage(errorMessage);
    }
    return isValid;
  }

  /**
   * Method to validate the tag API parameters
   * 
   * @param naId
   *          NARA ID
   * @param objectId
   *          Object ID
   * @param tagText
   *          Tag annotation text value
   * @param tagLength
   *          Tag annotation text length value
   * @param format
   *          OPA Response Object Format
   */
  public void validate(String naId, String objectId, String tagText,
      int tagLength, String format) {
    isValid = true;

    if (!validateNaid(naId))
      return;
    if (!validateObjectId(objectId))
      return;
    if (!validateTagText(tagText))
      return;
    if (!validateTagText(tagText))
      return;
    if (!validateTagLength(tagLength))
      return;
    if (!validateFormat(format))
      return;
  }

  /**
   * NAID parameter check (cannot be empty)
   * 
   * @param naId
   *          NAID
   * @return isValid (true/false)
   */
  public boolean validateNaid(String naId) {
    if (naId == null || naId.isEmpty() || naId.length() > NumericConstants.NA_ID_LENGTH) {
      isValid = false;
      errorCode = TagErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Invalid naId value");
    }
    return isValid;
  }

  /**
   * Object ID parameter check
   * 
   * @param objectId
   *          Object ID value
   * @return isValid (true/false)
   */
  public boolean validateObjectId(String objectId) {
    if (objectId == null || objectId.length() > NumericConstants.OBJECT_ID_LENGTH) {
      isValid = false;
      errorCode = TagErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Invalid objectId value");
    }
    return isValid;
  }

  /**
   * Tag text parameter check (cannot be empty, contain embedded HTML or invalid
   * characters)
   * 
   * @param tagText
   *          tag annotation
   * @return isValid (true/false)
   */
  public boolean validateTagText(String tagText) {
    if (tagText == null || tagText.isEmpty()) {

      // tag cannot be empty
      isValid = false;
      errorCode = TagErrorCode.INVALID_PARAMETER;
      getErrorCode().setErrorMessage("Tag cannot be empty");
      ;
    } else if (tagText.isEmpty()) {

      // tag cannot contain embedded HTML
      isValid = false;
      errorCode = TagErrorCode.EMBEDDED_HTML;
      getErrorCode().setErrorMessage("Tag cannot contain embedded HTML");

    } else if (tagText.isEmpty()) {

      // tag cannot contain embedded HTML
      isValid = false;
      errorCode = TagErrorCode.INVALID_CHARACTERS;
      getErrorCode().setErrorMessage("Tag cannot contain invalid characters");
    }
    return isValid;
  }

  /**
   * Tag Length (must be less than config maxTagLength)
   * 
   * @param tagLength
   *          length of tag annotation
   * @return isValid (true/false)
   */
  public boolean validateTagLength(int tagLength) {

    Config config = configService.getConfig(configFilePath);
    if (config == null) {
      isValid = false;
      errorCode = TagErrorCode.CONFIG_FILE_NOT_FOUND;
      getErrorCode().setErrorMessage("The config.xml file was not found");
    } else if (tagLength > config.getTagsLength()
        || tagLength > NumericConstants.TAG_TEXT_LENGTH) {
      isValid = false;
      errorCode = TagErrorCode.TAG_SIZE_EXCEEDED;
      getErrorCode().setErrorMessage("Tag length is too long");
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
      errorCode = TagErrorCode.INVALID_PARAMETER;
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

  public TagErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(TagErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public void setStandardError(TagErrorCode errorCode) {
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
      case DUPLICATE_ANNOTATION:
        getErrorCode().setErrorMessage(
            "A tag you are trying to add already exists");
        break;
      case CREATE_TAG_FAILED:
        getErrorCode().setErrorMessage(
            "The tag(s) were not created - tagService-createTag returned NULL");
        break;
      case TAG_NOT_FOUND:
        getErrorCode().setErrorMessage("The tag was not found");
        break;
      case NO_TAGS_FOUND:
        getErrorCode().setErrorMessage("No tags were found");
        break;
      default:
        getErrorCode().setErrorMessage("Internal error");
        break;
    }
  }

}
