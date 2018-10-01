package gov.nara.opa.api.annotation.locks;

import java.sql.Timestamp;

/**
 * AnnotationLock entity class
 */
public class AnnotationLock {

  private int lockId;
  private int accountId;
  private String naId;
  private String objectId;
  private String opaId;
  private String languageISO;
  private Timestamp lockTS;

  public AnnotationLock() {
  }

  public AnnotationLock(int lockId, int accountId, String naId,
      String objectId, String opaId, String languageISO, Timestamp lockTS) {
    this.lockId = lockId;
    this.accountId = accountId;
    this.naId = naId;
    this.objectId = objectId;
    this.opaId = opaId;
    this.languageISO = languageISO;
    this.lockTS = lockTS;
  }

  public static AnnotationLock copy(AnnotationLock sourceAnnotationLock) {
    AnnotationLock annotationLock = new AnnotationLock();

    annotationLock.setLockId(sourceAnnotationLock.getLockId());
    annotationLock.setAccountId(sourceAnnotationLock.getAccountId());
    annotationLock.setNaId(sourceAnnotationLock.getNaId());
    annotationLock.setObjectId(sourceAnnotationLock.getObjectId());
    annotationLock.setOpaId(sourceAnnotationLock.getOpaId());
    annotationLock.setLanguageISO(sourceAnnotationLock.getLanguageISO());
    annotationLock.setLockTS(sourceAnnotationLock.getLockTS());

    return annotationLock;
  }

  public int getLockId() {
    return lockId;
  }

  public void setLockId(int lockId) {
    this.lockId = lockId;
  }

  public int getAccountId() {
    return accountId;
  }

  public void setAccountId(int accountId) {
    this.accountId = accountId;
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

  public String getOpaId() {
    return opaId;
  }

  public void setOpaId(String opaId) {
    this.opaId = opaId;
  }

  public String getLanguageISO() {
    return languageISO;
  }

  public void setLanguageISO(String languageISO) {
    this.languageISO = languageISO;
  }

  public Timestamp getLockTS() {
    return lockTS;
  }

  public void setLockTS(Timestamp lockTS) {
    this.lockTS = lockTS;
  }

  @Override
  public String toString() {

    return String
        .format(
            "Lock: LockId=%1$d, AccountId=%2$d, NaId=%3$s, ObjectId=%4$s, OpaId=%5$s, LanguageISO=%6$s, LockTS=%7$s",
            lockId, accountId, naId, objectId, opaId, languageISO,
            (lockTS != null ? lockTS.toString() : "null"));

  }

}
