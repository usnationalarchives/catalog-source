package gov.nara.opa.api.annotation;

import gov.nara.opa.common.valueobject.annotation.Annotation;

import java.sql.Timestamp;

public class TranscriptedOpaTitle extends Annotation {

  public TranscriptedOpaTitle() {

  }

  private String naId;
  private String opaTitle;
  private String opaType;
  private String objectId;
  private String fullName;
  private String creatorFullName;
  private String creatorUserName;
  private int totalPages;
  private int pageNum;
  private int accountId;
  private int annotationId;
  private int firstAnnotationId;
  private Timestamp addedTs;
  private Timestamp creationTs;

  public TranscriptedOpaTitle(String naId, String opaTitle, String opaType,
      String objectId, int totalPages, Timestamp addedTs) {

    this.naId = naId;
    this.opaTitle = opaTitle;
    this.opaType = opaType;
    this.objectId = objectId;
    this.totalPages = totalPages;
    this.addedTs = addedTs;
  }

  /**
   * @return the fullName
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * @param fullName
   *          the fullName to set
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  /**
   * @return the accountId
   */
  public int getAccountId() {
    return accountId;
  }

  /**
   * @param accountId
   *          the accountId to set
   */
  public void setAccountId(int accountId) {
    this.accountId = accountId;
  }

  /**
   * @return the annotationId
   */
  public int getAnnotationId() {
    return annotationId;
  }

  /**
   * @param annotationId
   *          the annotationId to set
   */
  public void setAnnotationId(int annotationId) {
    this.annotationId = annotationId;
  }

  public int getFirstAnnotationId() {
    return firstAnnotationId;
  }

  public void setFirstAnnotationId(int firstAnnotationId) {
    this.firstAnnotationId = firstAnnotationId;
  }

  /**
   * @return the pageNum
   */
  public int getPageNum() {
    return pageNum;
  }

  /**
   * @param pageNum
   *          the pageNum to set
   */
  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }

  /**
   * @return the naId
   */
  public String getNaId() {
    return naId;
  }

  /**
   * @param naId
   *          the naId to set
   */
  public void setNaId(String naId) {
    this.naId = naId;
  }

  /**
   * @return the opaTitle
   */
  public String getOpaTitle() {
    return opaTitle;
  }

  /**
   * @param opaTitle
   *          the opaTitle to set
   */
  public void setOpaTitle(String opaTitle) {
    this.opaTitle = opaTitle;
  }

  /**
   * @return the opaType
   */
  public String getOpaType() {
    return opaType;
  }

  /**
   * @param opaType
   *          the opaType to set
   */
  public void setOpaType(String opaType) {
    this.opaType = opaType;
  }

  /**
   * @return the objectId
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * @param objectId
   *          the objectId to set
   */
  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  /**
   * @return the totalPages
   */
  public int getTotalPages() {
    return totalPages;
  }

  /**
   * @param totalPages
   *          the totalPages to set
   */
  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  /**
   * @return the addedTs
   */
  public Timestamp getAddedTs() {
    return addedTs;
  }

  /**
   * @param addedTs
   *          the addedTs to set
   */
  public void setAddedTs(Timestamp addedTs) {
    this.addedTs = addedTs;
  }

  public String getCreatorFullName() {
    return creatorFullName;
  }

  public void setCreatorFullName(String creatorFullName) {
    this.creatorFullName = creatorFullName;
  }

  public String getCreatorUserName() {
    return creatorUserName;
  }

  public void setCreatorUserName(String creatorUserName) {
    this.creatorUserName = creatorUserName;
  }

  public Timestamp getCreationTs() {
    return creationTs;
  }

  public void setCreationTs(Timestamp creationTs) {
    this.creationTs = creationTs;
  }

}
