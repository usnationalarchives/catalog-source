package gov.nara.opa.api.search;

import java.util.ArrayList;

public class ViewListSearchResults extends Search {

  public ViewListSearchResults() {

  }

  public ViewListSearchResults(int docNumber, String naId, String opaId,
      String url, String localId, ArrayList<String> hmsEntryNumbers,
      ArrayList<String> containerId, String iconType, String title,
      String titleDate, String parentLevel, String parentTitle,
      ArrayList<String> creators, String thumbnailFile) {
    this.docNumber = docNumber;
    this.naId = naId;
    this.opaId = opaId;
    this.url = url;
    this.localId = localId;
    this.hmsEntryNumbers = hmsEntryNumbers;
    this.containerId = containerId;
    this.iconType = iconType;
    this.title = title;
    this.titleDate = titleDate;
    this.parentLevel = parentLevel;
    this.parentTitle = parentTitle;
    this.creators = creators;
    this.thumbnailFile = thumbnailFile;
  }

}
