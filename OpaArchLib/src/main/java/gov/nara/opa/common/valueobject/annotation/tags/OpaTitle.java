package gov.nara.opa.common.valueobject.annotation.tags;

import gov.nara.opa.common.valueobject.annotation.Annotation;

import java.sql.Timestamp;

public class OpaTitle extends Annotation {

  public OpaTitle() {

  }

  private String naId;
  private String opaTitle;
  private String opaType;
  private String objectId;
  private int totalPages;
  private int pageNum;
  private Timestamp addedTs;

  public OpaTitle(String naId, String opaTitle, String opaType,
      String objectId, int totalPages, Timestamp addedTs) {

    this.naId = naId;
    this.opaTitle = opaTitle;
    this.opaType = opaType;
    this.objectId = objectId;
    this.totalPages = totalPages;
    this.addedTs = addedTs;
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

}
