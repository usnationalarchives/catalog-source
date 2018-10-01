package gov.nara.opa.api.validation.moderator;

import gov.nara.opa.api.dataaccess.moderator.AnnotationReasonDao;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.valueobject.moderator.AnnotationReasonValueObject;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.validation.moderator.TagsModeratorRequestParameters;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TagsDeleteModeratorValidator extends OpaApiAbstractValidator {

  @Autowired
  TagDao tagDao;

  @Autowired
  AnnotationReasonDao annotationReasonDao;

  public static final String TAG_VALUE_OBJECT_KEY = "tag";

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    TagsModeratorRequestParameters requestParameters = (TagsModeratorRequestParameters) validationResult
        .getValidatedRequest();
    List<TagValueObject> tags = tagDao.selectAllTags(
        requestParameters.getNaId(), requestParameters.getObjectId(),
        requestParameters.getText(), true);
    validateTagExists(tags, validationResult);
    if (!validationResult.isValid()) {
      return;
    }
    TagValueObject foundTag = tags.get(0);
    validationResult.addContextObject(TAG_VALUE_OBJECT_KEY, foundTag);
    validateReasonId(foundTag, validationResult,
        requestParameters.getReasonId());
  }

  private void validateTagExists(List<TagValueObject> tags,
      ValidationResult validationResult) {
    if (tags == null || tags.size() == 0) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.TAG_NOT_FOUND);
      error.setErrorMessage(ErrorConstants.ACTIVE_TAG_NOT_FOUND);
      validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
      validationResult.addCustomValidationError(error);
    }
  }

  private void validateReasonId(TagValueObject tag,
      ValidationResult validationResult, Integer reasonId) {

    AnnotationReasonValueObject annotationReason = annotationReasonDao
        .getAnnotationReasonById(reasonId);

    if (annotationReason == null || !annotationReason.getStatus()) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
      error.setErrorMessage(String.format(
          ErrorConstants.REASON_ID_DOES_NOT_EXIST, reasonId));
      validationResult.addCustomValidationError(error);
    }
  }

  @Override
  protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
    // TODO Auto-generated method stub
    return null;
  }

}
