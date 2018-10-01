package gov.nara.opa.api.moderator;

import java.sql.Timestamp;

/**
 * Class AnnotationReason with the required fields and columns as the table on
 * the database.
 */
public class AnnotationReason {
  private int reasonId;
  private int accountId;
  private String reason;
  private int status;
  private Timestamp addedTs;

  public AnnotationReason() {
  }

  /**
   * Nondefault constructor
   * 
   * @param accountId
   * @param reason
   * @param status
   * @param addedTs
   */
  public AnnotationReason(int accountId, String type, String reason,
      int status, Timestamp addedTs) {
    this.accountId = accountId;
    this.reason = reason;
    this.status = status;
    this.addedTs = addedTs;
  }

  /**
   * @return the reasonId
   */
  public int getReasonId() {
    return reasonId;
  }

  /**
   * @param reasonId
   *          the reasonId to set
   */
  public void setReasonId(int reasonId) {
    this.reasonId = reasonId;
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
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

  /**
   * @param reason
   *          the reason to set
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(int status) {
    this.status = status;
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
