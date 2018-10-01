package gov.nara.opa.api.validation.annotation.tags;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObjectConstants;

import java.util.LinkedHashMap;

public class TagsDeleteRequestParameters extends AbstractRequestParameters
    implements TagValueObjectConstants {

  @OpaNotNullAndNotEmpty
  String text;

  @OpaNotNullAndNotEmpty
  @OpaSize(max = FieldConstraintConstants.MAX_NAID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  String naId;

  @OpaSize(max = FieldConstraintConstants.MAX_OBJECTID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  String objectId;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getNaId() {
    return naId;
  }

  public void setNaId(String naId) {
    this.naId = naId;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(NA_ID_REQ_ASP, getNaId());
    if (getObjectId() != null && !getObjectId().isEmpty()) {
      requestParams.put(OBJECT_ID_REQ_ASP, getObjectId());
    }
    if (getText() != null && !getText().isEmpty()) {
      requestParams.put(TAG_TEXT_REQ_ASP, getText());
    }

    return requestParams;
  }
}
