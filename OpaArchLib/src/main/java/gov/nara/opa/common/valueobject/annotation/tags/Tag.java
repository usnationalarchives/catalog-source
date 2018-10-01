package gov.nara.opa.common.valueobject.annotation.tags;

import gov.nara.opa.common.valueobject.annotation.Annotation;

import java.sql.Timestamp;

public class Tag extends Annotation {

  public static final String ANNOTATION_TYPE = "TG";

  private String annotationMD5;

  public Tag() {

  }

  public Tag(int total, int annotationId, String annotation,
      String annotationMD5, boolean status, String naId, String objectId,
      int pageNum, String opaId, int accountId, String userName,
      String fullName, boolean isNaraStaff, Timestamp annotationTS) {
    this.total = total;
    this.annotationId = annotationId;
    this.annotation = annotation;
    this.annotationMD5 = annotationMD5;
    this.status = status;
    this.naId = naId;
    this.objectId = objectId;
    this.pageNum = pageNum;
    this.opaId = opaId;
    this.accountId = accountId;
    this.userName = userName;
    this.fullName = fullName;
    this.isNaraStaff = isNaraStaff;
    this.annotationTS = annotationTS;
  }

  public String getAnnotationMD5() {
    return annotationMD5;
  }

  public void setAnnotationMD5(String annotationMD5) {
    this.annotationMD5 = annotationMD5;
  }

}
