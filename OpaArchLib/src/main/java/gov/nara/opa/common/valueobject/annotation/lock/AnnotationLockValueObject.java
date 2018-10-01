package gov.nara.opa.common.valueobject.annotation.lock;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

public class AnnotationLockValueObject extends AbstractWebEntityValueObject
    implements AnnotationLockValueObjectConstants {

  private int lockId;
  private int accountId;
  private String naId;
  private String objectId;
  private String opaId;
  private String languageISO;
  private Timestamp lockTS;

  private UserAccountValueObject user;

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

  public UserAccountValueObject getUser() {
    return user;
  }

  public void setUser(UserAccountValueObject user) {
    this.user = user;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    LinkedHashMap<String, Object> databaseContent = new LinkedHashMap<String, Object>();

    databaseContent.put(LOCK_ID_DB, getLockId());
    databaseContent.put(ACCOUNT_ID_DB, getAccountId());
    databaseContent.put(NA_ID_DB, getNaId());
    databaseContent.put(OBJECT_ID_DB, getObjectId());
    databaseContent.put(OPA_ID_DB, getOpaId());
    databaseContent.put(LANGUAGE_ISO_DB, getLanguageISO());
    databaseContent.put(LOCK_TS_DB, getLockTS());
    
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
	  return null;
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
