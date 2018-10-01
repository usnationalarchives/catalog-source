package gov.nara.opa.api.search;

public class FullResults extends Search {

  public FullResults() {

  }

  public FullResults(int docNumber, Float score, String type, String naId,
      String opaId, String description, String authority, String objects) {
    this.docNumber = docNumber;
    this.score = score;
    this.type = type;
    this.naId = naId;
    this.opaId = opaId;
    this.description = description;
    this.authority = authority;
    this.objects = objects;
  }

}
