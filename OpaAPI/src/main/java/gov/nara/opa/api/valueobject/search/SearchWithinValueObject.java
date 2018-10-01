package gov.nara.opa.api.valueobject.search;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchWithinValueObject extends AbstractWebEntityValueObject
    implements SearchWithinObjectConstants {

  int docNumber;
  Float score;
  String naId;
  String opaId;
  String title;
  String hierachy;
  String url;
  String iconType;
  String thumbnailFile;
  boolean hasOnline;
  String teaser;
  List<String> tabType;
  HashMap<String, ArrayList<Map<String, Object>>> briefResults;

  public int getDocNumber() {
    return docNumber;
  }

  public void setDocNumber(int docNumber) {
    this.docNumber = docNumber;
  }

  public Float getScore() {
    return score;
  }

  public void setScore(Float score) {
    this.score = score;
  }

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

  public String getHierachy() {
    return hierachy;
  }

  public void setHierachy(String hierachy) {
    this.hierachy = hierachy;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getIconType() {
    return iconType;
  }

  public void setIconType(String iconType) {
    this.iconType = iconType;
  }

  public String getThumbnailFile() {
    return thumbnailFile;
  }

  public void setThumbnailFile(String thumbnailFile) {
    this.thumbnailFile = thumbnailFile;
  }

  public boolean getHasOnline() {
    return hasOnline;
  }

  public void setHasOnline(boolean hasOnline) {
    this.hasOnline = hasOnline;
  }

  public String getTeaser() {
    return teaser;
  }

  public void setTeaser(String teaser) {
    this.teaser = teaser;
  }

  public List<String> getTabType() {
    return tabType;
  }

  public void setTabType(List<String> tabType) {
    this.tabType = tabType;
  }

  public HashMap<String, ArrayList<Map<String, Object>>> getBriefResults() {
    return briefResults;
  }

  public void setBriefResults(
      HashMap<String, ArrayList<Map<String, Object>>> briefResults) {
    this.briefResults = briefResults;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

    aspireContent.put(NUM_ASP, getDocNumber());
    aspireContent.put(SCORE_ASP, getScore());
    aspireContent.put(NA_ID_ASP, getNaId());
    aspireContent.put(OPA_ID_ASP, getOpaId());

    if (getTitle() != null) {
      aspireContent.put(TITLE_ASP, getTitle());
    }
    if (getHierachy() != null) {
      aspireContent.put(HIERACHY_ASP, getHierachy());
    }
    if (getUrl() != null) {
      aspireContent.put(URL_ASP, getUrl());
    }
    if (getIconType() != null) {
      aspireContent.put(ICON_TYPE_ASP, getIconType());
    }
    if (getThumbnailFile() != null) {
      aspireContent.put(THUMBNAIL_ASP, getThumbnailFile());
    }
    aspireContent.put(HAS_ONLINE_ASP, getHasOnline());
    if (getTeaser() != null) {
      aspireContent.put(TEASER_ASP, getTeaser());
    }
    if (getTabType() != null) {
      aspireContent.put(TAB_TYPE_ASP, getTabType());
    }
    if (getBriefResults() != null) {
      aspireContent.put(BRIEF_RESULTS_ASP, getBriefResults());
    }

    return aspireContent;
  }

}
