package gov.nara.opa.api.valueobject.administrator;

import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AccountReasonValueObject extends AbstractWebEntityValueObject
    implements AccountReasonValueObjectConstants {

  private Integer reasonId;
  private Integer accountId;
  private String reason;
  private Boolean reasonStatus;
  private Timestamp reasonAddedTs;

  @Override
  public Map<String, Object> getDatabaseContent() {
    Map<String, Object> databaseContent = new HashMap<String, Object>();
    databaseContent.put(REASON_ID_DB, getReasonId());
    databaseContent.put(ACCOUNT_ID_DB, getAccountId());
    databaseContent.put(REASON_DB, getReason());
    databaseContent.put(REASON_STATUS_DB, getReasonStatus());
    databaseContent.put(REASON_ADDED_TS_DB, getReasonAddedTs());
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    aspireContent.put(REASON_ID_ASP, getReasonId());
    aspireContent.put(ACCOUNT_ID_ASP, getAccountId());
    aspireContent.put(REASON_ASP, getReason());
    aspireContent.put(STATUS_ASP, getReasonStatus());
    aspireContent.put(CREATED_ASP,
        TimestampUtils.getUtcString(getReasonAddedTs()));
    return aspireContent;
  }

  public Integer getReasonId() {
    return reasonId;
  }

  public void setReasonId(Integer reasonId) {
    this.reasonId = reasonId;
  }

  public Integer getAccountId() {
    return accountId;
  }

  public void setAccountId(Integer accountId) {
    this.accountId = accountId;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Boolean getReasonStatus() {
    return reasonStatus;
  }

  public void setReasonStatus(Boolean reasonStatus) {
    this.reasonStatus = reasonStatus;
  }

  public Timestamp getReasonAddedTs() {
    return reasonAddedTs;
  }

  public void setReasonAddedTs(Timestamp reasonAddedTs) {
    this.reasonAddedTs = reasonAddedTs;
  }
}
