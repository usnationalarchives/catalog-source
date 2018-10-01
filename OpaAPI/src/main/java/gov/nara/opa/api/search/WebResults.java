package gov.nara.opa.api.search;

public class WebResults extends Search {

  public WebResults() {

  }

  public WebResults(int docNumber, Float score, String naId, String opaId,
      String title, String webArea, String webAreaUrl, String url,
      String iconType, String teaser) {
    this.docNumber = docNumber;
    this.score = score;
    this.naId = naId;
    this.opaId = opaId;
    this.title = title;
    this.webArea = webArea;
    this.webAreaUrl = webAreaUrl;
    this.url = url;
    this.iconType = iconType;
    this.teaser = teaser;
  }
}
