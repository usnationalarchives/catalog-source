package gov.nara.opa.api.valueobject.moderator;

import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnnotationReasonValueObject extends AbstractWebEntityValueObject
    implements AnnotationReasonValueObjectConstants {

  private Integer reasonId;
  private Integer accountId;
  private String type;
  private String reason;
  private Boolean status;
  private Timestamp addedTS;

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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  public Timestamp getAddedTS() {
    return addedTS;
  }

  public void setAddedTS(Timestamp addedTS) {
    this.addedTS = addedTS;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    Map<String, Object> databaseContent = new LinkedHashMap<String, Object>();
    databaseContent.put(ACCOUNT_ID_DB, getAccountId());
    databaseContent.put(REASON_ADDED_TS_DB,
        TimestampUtils.getUtcString(getAddedTS()));
    databaseContent.put(REASON_DB, getReason());
    databaseContent.put(REASON_ID_DB, getReasonId());
    databaseContent.put(REASON_STATUS_DB, getStatus());
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }
}
