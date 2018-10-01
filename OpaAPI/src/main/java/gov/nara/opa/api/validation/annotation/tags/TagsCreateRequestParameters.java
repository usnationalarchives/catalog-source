package gov.nara.opa.api.validation.annotation.tags;

import gov.nara.opa.api.validation.common.propertyeditor.OpaArrayListPropertyEditor;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObjectConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TagsCreateRequestParameters extends AbstractRequestParameters
    implements TagValueObjectConstants {

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.textList = OpaArrayListPropertyEditor.getTokens(text);
    this.text = text;
  }

  @OpaNotNullAndNotEmpty
  private String text;

  private ArrayList<String> textList;

  @OpaNotNullAndNotEmpty
  @OpaSize(max = FieldConstraintConstants.MAX_NAID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  private String naId;

  @OpaSize(max = FieldConstraintConstants.MAX_OBJECTID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  private String objectId;
  
  private int pageNum;

  public ArrayList<String> getTextList() {
    return textList;
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
    requestParams.put(PAGE_NUM_REQ_ASP, getPageNum());
    requestParams.put(TAG_TEXT_REQ_ASP, getTextList());
    return requestParams;
  }

  public void setTextList(ArrayList<String> textList) {
    this.textList = textList;
  }

  public int getPageNum() {
    return pageNum;
  }

  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }
}
