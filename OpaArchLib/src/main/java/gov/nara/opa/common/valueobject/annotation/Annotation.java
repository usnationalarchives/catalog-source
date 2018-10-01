/**
 * 
 */
package gov.nara.opa.common.valueobject.annotation;

import java.sql.Timestamp;

public class Annotation {
  protected int total;
  protected int annotationId;
  protected String annotation;
  protected boolean status;
  protected String naId;
  protected String objectId;
  protected int pageNum;
  protected String opaId;
  protected int accountId;
  protected String userName;
  protected String fullName;
  protected boolean isNaraStaff;
  protected Timestamp annotationTS;

  public Annotation() {

  }

  public Annotation(int total, int annotationId, String annotation,
      boolean status, String naId, String objectId, int pageNum, String opaId,
      int accountId, String userName, String fullName, boolean isNaraStaff,
      Timestamp annotationTS) {
    this.total = total;
    this.annotationId = annotationId;
    this.annotation = annotation;
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

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getAnnotationId() {
    return annotationId;
  }

  public void setAnnotationId(int annotationId) {
    this.annotationId = annotationId;
  }

  public String getAnnotation() {
    return annotation;
  }

  public void setAnnotation(String annotation) {
    this.annotation = annotation;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
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

  public int getPageNum() {
    return pageNum;
  }

  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }

  public String getOpaId() {
    return opaId;
  }

  public void setOpaId(String opaId) {
    this.opaId = opaId;
  }

  public int getAccountId() {
    return accountId;
  }

  public void setAccountId(int accountId) {
    this.accountId = accountId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public boolean getIsNaraStaff() {
    return isNaraStaff;
  }

  public void setIsNaraStaff(boolean isNaraStaff) {
    this.isNaraStaff = isNaraStaff;
  }

  public Timestamp getAnnotationTS() {
    return annotationTS;
  }

  public void setAnnotationTS(Timestamp annotationTS) {
    this.annotationTS = annotationTS;
  }

}
