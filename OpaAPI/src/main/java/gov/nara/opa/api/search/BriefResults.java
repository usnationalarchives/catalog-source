package gov.nara.opa.api.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BriefResults extends Search {

  // Brief Results
  HashMap<String, ArrayList<Map<String, Object>>> documentBriefResults;

  public BriefResults() {

  }

  public BriefResults(int docNumber, Float score, String naId, String opaId,
      String url, String iconType, String thumbnailFile, boolean hasOnline,
      List<String> tabType, String teaser,
      HashMap<String, ArrayList<Map<String, Object>>> documentBriefResults) {
    this.docNumber = docNumber;
    this.score = score;
    this.naId = naId;
    this.opaId = opaId;
    this.url = url;
    this.iconType = iconType;
    this.thumbnailFile = thumbnailFile;
    this.hasOnline = hasOnline;
    this.tabType = tabType;
    this.teaser = teaser;
    this.documentBriefResults = documentBriefResults;
  }

  public HashMap<String, ArrayList<Map<String, Object>>> getDocumentBriefResults() {
    return documentBriefResults;
  }

  public void setDocumentBriefResults(
      HashMap<String, ArrayList<Map<String, Object>>> documentBriefResults) {
    this.documentBriefResults = documentBriefResults;
  }

}
