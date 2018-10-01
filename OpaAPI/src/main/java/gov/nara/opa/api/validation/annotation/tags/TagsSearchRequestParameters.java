package gov.nara.opa.api.validation.annotation.tags;

import gov.nara.opa.api.validation.common.propertyeditor.OpaArrayListPropertyEditor;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObjectConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TagsSearchRequestParameters extends AbstractRequestParameters
    implements TagValueObjectConstants {

  private Map<String, String[]> queryParameters;

  @Override
  public boolean bypassExtraneousHttpParametersValidation() {
    return true;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String text) {
    this.textList = OpaArrayListPropertyEditor.getTokens(text);
    this.tag = text;
  }

  @OpaNotNullAndNotEmpty
  String tag;

  ArrayList<String> textList;

  public ArrayList<String> getTextList() {
    return textList;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(TAG_TEXT_REQ_ASP, getTextList());
    return requestParams;
  }

  public void setTextList(ArrayList<String> textList) {
    this.textList = textList;
  }

  public Map<String, String[]> getQueryParameters() {
    return queryParameters;
  }

  public void setQueryParameters(Map<String, String[]> queryParameters) {
    this.queryParameters = new HashMap<String, String[]>();
    this.queryParameters.putAll(queryParameters);
  }

}
