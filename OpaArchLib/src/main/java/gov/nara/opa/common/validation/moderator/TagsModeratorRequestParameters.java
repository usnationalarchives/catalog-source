package gov.nara.opa.common.validation.moderator;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;

import java.util.LinkedHashMap;

public class TagsModeratorRequestParameters extends AbstractRequestParameters {

  private String apiType;
  
  @OpaNotNullAndNotEmpty
  private Integer reasonId;

  private String notes;

  @OpaNotNullAndNotEmpty
  private String text;

  @OpaNotNullAndNotEmpty
  @OpaSize(max = FieldConstraintConstants.MAX_NAID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  private String naId;

  @OpaSize(max = FieldConstraintConstants.MAX_OBJECTID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  private String objectId;

  public String getApiType() {
    return apiType;
  }

  public void setApiType(String apiType) {
    this.apiType = apiType;
  }

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

  public Integer getReasonId() {
    return reasonId;
  }

  public void setReasonId(Integer reasonId) {
    this.reasonId = reasonId;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put("naId", naId);
    if (objectId != null && !objectId.isEmpty()) {
      requestParams.put("objectId", objectId);
    }
    if (text != null && !text.isEmpty()) {
      requestParams.put("tagText", text);
    }
    if (reasonId != null) {
      requestParams.put("reasonId", reasonId);
    }
    if (notes != null && !notes.isEmpty()) {
      requestParams.put("notes", notes);
    } else {
      requestParams.put("notes", "");
    }
    return requestParams;
  }
}
