package gov.nara.opa.api.annotation.transcriptions;

import gov.nara.opa.common.valueobject.annotation.Annotation;

import java.sql.Timestamp;

public class Transcription extends Annotation {

  private int savedVersNum;
  private String annotationMD5;
  private int firstAnnotationId;

  public Transcription() {

  }

  public Transcription(int total, int annotationId, int firstAnnotationId,
      int savedVersNum, String annotation, String annotationMD5,
      boolean status, String naId, String objectId, int pageNum, String opaId,
      int accountId, String userName, String fullName, boolean isNaraStaff,
      Timestamp annotationTS) {
    this.total = total;
    this.annotationId = annotationId;
    this.firstAnnotationId = firstAnnotationId;
    this.savedVersNum = savedVersNum;
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

  public int getSavedVersNum() {
    return savedVersNum;
  }

  public void setSavedVersNum(int savedVersNum) {
    this.savedVersNum = savedVersNum;
  }

  public String getAnnotationMD5() {
    return annotationMD5;
  }

  public void setAnnotationMD5(String annotationMD5) {
    this.annotationMD5 = annotationMD5;
  }

  public int getFirstAnnotationId() {
    return firstAnnotationId;
  }

  public void setFirstAnnotationId(int firstAnnotationId) {
    this.firstAnnotationId = firstAnnotationId;
  }

  @Override
  public String toString() {
    return String.format("Transcription: id=%1$d, timestamp=%2$s, text=%3$s ",
        annotationId, annotationTS.toString(), annotation);
  }

}
