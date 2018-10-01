package gov.nara.opa.api.valueobject.search;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContentDetailValueObject extends AbstractWebEntityValueObject
    implements ContentDetailObjectConstants {

  private String naId;
  private String opaId;
  private String title;
  private String sourceType;
  private LinkedHashMap<String, Object> content;
  private Long totalCount;
  private String prevNaId;
  private String nextNaId;
  private String shortContent;

  public String getNaId() {
    return naId;
  }

  public void setNaId(String naId) {
    this.naId = naId;
  }

  public String getOpaId() {
    return opaId;
  }

  public void setOpaId(String opaId) {
    this.opaId = opaId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public LinkedHashMap<String, Object> getContent() {
    return content;
  }

  public void setContent(LinkedHashMap<String, Object> content) {
    this.content = content;
  }

  public Long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Long totalCount) {
    this.totalCount = totalCount;
  }

  public String getPrevNaId() {
    return prevNaId;
  }

  public void setPrevNaId(String prevNaId) {
    this.prevNaId = prevNaId;
  }

  public String getNextNaId() {
    return nextNaId;
  }

  public void setNextNaId(String nextNaId) {
    this.nextNaId = nextNaId;
  }

  public String getShortContent() {
    return shortContent;
  }

  public void setShortContent(String shortContent) {
    this.shortContent = shortContent;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

    aspireContent.put(TOTAL_RECORDS_ASP, getTotalCount());
    aspireContent.put(NA_ID_ASP, getNaId());
    aspireContent.put(PREV_NA_ID_ASP, getPrevNaId());
    aspireContent.put(NEXT_NA_ID_ASP, getNextNaId());
    aspireContent.put(OPA_ID_ASP, getOpaId());
    aspireContent.put(TITLE_ASP, getTitle());
    aspireContent.put(SHORT_CONTENT_ASP, getShortContent());

    if (getContent() != null) {
      aspireContent.put(CONTENT_ASP, getContent());
    }

    return aspireContent;
  }

}
