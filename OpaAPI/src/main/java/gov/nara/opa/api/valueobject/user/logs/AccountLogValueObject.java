package gov.nara.opa.api.valueobject.user.logs;

import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AccountLogValueObject extends AbstractWebEntityValueObject
    implements AccountLogValueObjectConstants {

  private Integer logId;
  private Integer accountId;
  private Integer adminAccountId;
  private String adminUserName;
  private Boolean status;
  private String action;
  private Integer reasonId;
  private String reason;
  private String notes;
  private Timestamp logTs;
  private Boolean accountStatus;
  private Boolean accountHasNotes;
  private String adminFullName;

  @Override
  public Map<String, Object> getDatabaseContent() {
    Map<String, Object> databaseContent = new HashMap<String, Object>();

    databaseContent.put(ACCOUNT_ID_DB, getAccountId());
    databaseContent.put(ACTION_DB, getAction());
    databaseContent.put(ADMIN_ACCOUNT_ID_DB, getAdminAccountId());
    databaseContent.put(LOG_ID_DB, getLogId());
    databaseContent.put(LOG_TS_DB, getLogTs());
    databaseContent.put(NOTES_DB, getNotes());
    databaseContent.put(REASON_ID_DB, getReasonId());
    databaseContent.put(STATUS_DB, getStatus());
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    aspireContent.put(ADMIN_ID_ASP, getAdminAccountId());
    aspireContent.put(ADMIN_USER_NAME_ASP, getAdminUserName());
    aspireContent.put(ACTION_ASP, getAction());
    aspireContent.put(LOG_TS_ASP, TimestampUtils.getUtcString(getLogTs()));
    aspireContent.put(REASON_ASP, getReason());
    aspireContent.put(REASON_ID_ASP, getReasonId());
    aspireContent.put(NOTE_ASP, getNotes());
    aspireContent.put(ACCOUNT_STATUS_ASP, getAccountStatus() ? ACTIVE_CODE
        : INACTIVE_CODE);
    aspireContent.put(HAS_NOTE_ASP, getAccountHasNotes());
    aspireContent.put(ADMIN_FULL_NAME_ASP, getAdminFullName());
    return aspireContent;
  }

  public Integer getLogId() {
    return logId;
  }

  public void setLogId(Integer logId) {
    this.logId = logId;
  }

  public Integer getAccountId() {
    return accountId;
  }

  public void setAccountId(Integer accountId) {
    this.accountId = accountId;
  }

  public Integer getAdminAccountId() {
    return adminAccountId;
  }

  public void setAdminAccountId(Integer adminAccountId) {
    this.adminAccountId = adminAccountId;
  }

  public Boolean getStatus() {
    return status;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  public Integer getReasonId() {
    return reasonId;
  }

  public void setReasonId(Integer reasonId) {
    this.reasonId = reasonId;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Timestamp getLogTs() {
    return logTs;
  }

  public void setLogTs(Timestamp logTs) {
    this.logTs = logTs;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Boolean getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(Boolean accountStatus) {
    this.accountStatus = accountStatus;
  }

  public Boolean getAccountHasNotes() {
    return accountHasNotes;
  }

  public void setAccountHasNotes(Boolean accountHasNotes) {
    this.accountHasNotes = accountHasNotes;
  }

  public String getAdminUserName() {
    return adminUserName;
  }

  public void setAdminUserName(String adminUserName) {
    this.adminUserName = adminUserName;
  }

  public String getAdminFullName() {
    return adminFullName;
  }

  public void setAdminFullName(String adminFullName) {
    this.adminFullName = adminFullName;
  }
}
