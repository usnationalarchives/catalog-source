package gov.nara.opa.common.valueobject.annotation.logs;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnnotationLogValueObject extends AbstractWebEntityValueObject
    implements AnnotationLogValueObjectConstants {

  private Integer logId;
  private String annotationType;
  private Integer annotationId;
  private Integer firstAnnotationId;
  private Integer parentId;
  private Integer sequence;
  private String languageISO;
  private Integer versionNum;
  private String annotationMD5;
  private Boolean status;
  private String naId;
  private String objectId;
  private Integer pageNum;
  private String opaId;
  private Integer accountId;
  private String sessionId;
  private Integer affectsAccountId;
  private Integer firstAccountId;
  private String action;
  private Integer reasonId;
  private String notes;
  private Timestamp logTS;

  public Integer getLogId() {
    return logId;
  }

  public void setLogId(Integer logId) {
    this.logId = logId;
  }

  public String getAnnotationType() {
    return annotationType;
  }

  public void setAnnotationType(String annotationType) {
    this.annotationType = annotationType;
  }

  public Integer getAnnotationId() {
    return annotationId;
  }

  public void setAnnotationId(Integer annotationId) {
    this.annotationId = annotationId;
  }

  public Integer getFirstAnnotationId() {
    return firstAnnotationId;
  }

  public void setFirstAnnotationId(Integer firstAnnotationId) {
    this.firstAnnotationId = firstAnnotationId;
  }

  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public Integer getSequence() {
    return sequence;
  }

  public void setSequence(Integer sequence) {
    this.sequence = sequence;
  }

  public String getLanguageISO() {
    return languageISO;
  }

  public void setLanguageISO(String languageISO) {
    this.languageISO = languageISO;
  }

  public Integer getVersionNum() {
    return versionNum;
  }

  public void setVersionNum(Integer versionNum) {
    this.versionNum = versionNum;
  }

  public String getAnnotationMD5() {
    return annotationMD5;
  }

  public void setAnnotationMD5(String annotationMD5) {
    this.annotationMD5 = annotationMD5;
  }

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
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

  public Integer getPageNum() {
    return pageNum;
  }

  public void setPageNum(Integer pageNum) {
    this.pageNum = pageNum;
  }

  public String getOpaId() {
    return opaId;
  }

  public void setOpaId(String opaId) {
    this.opaId = opaId;
  }

  public Integer getAccountId() {
    return accountId;
  }

  public void setAccountId(Integer accountId) {
    this.accountId = accountId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public Integer getAffectsAccountId() {
    return affectsAccountId;
  }

  public void setAffectsAccountId(Integer affectsAccountId) {
    this.affectsAccountId = affectsAccountId;
  }

  public Integer getFirstAccountId() {
    return firstAccountId;
  }

  public void setFirstAccountId(Integer firstAccountId) {
    this.firstAccountId = firstAccountId;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
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

  public Timestamp getLogTS() {
    return logTS;
  }

  public void setLogTS(Timestamp logTS) {
    this.logTS = logTS;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    LinkedHashMap<String, Object> databaseContent = new LinkedHashMap<String, Object>();
    databaseContent.put(ACCOUNT_ID_DB, getAccountId());
    databaseContent.put(ACTION_DB, getAction());
    databaseContent.put(AFFECTS_ACCOUNT_ID_DB, getAffectsAccountId());
    databaseContent.put(ANNOTATION_ID_DB, getAnnotationId());
    databaseContent.put(ANNOTATION_TYPE_DB, getAnnotationType());
    databaseContent.put(ANNOTATION_MD5_DB, getAnnotationMD5());
    databaseContent.put(FIRST_ACCOUNT_ID_DB, getFirstAccountId());
    databaseContent.put(FIRST_ANNOTATION_ID_DB, getFirstAnnotationId());
    databaseContent.put(LANGUAGE_ISO_DB, getLanguageISO());
    databaseContent.put(LOG_ID_DB, getLogId());
    databaseContent.put(LOG_TS_DB, getLogTS());
    databaseContent.put(NA_ID_DB, getNaId());
    databaseContent.put(NOTES_DB, getNotes());
    databaseContent.put(OBJECT_ID_DB, getObjectId());
    databaseContent.put(OPA_ID_DB, getOpaId());
    databaseContent.put(PAGE_NUM_DB, getPageNum());
    databaseContent.put(PARENT_ID_DB, getParentId());
    databaseContent.put(REASON_ID_DB, getReasonId());
    databaseContent.put(SEQUENCE_DB, getSequence());
    databaseContent.put(SESSION_ID_DB, getSessionId());
    databaseContent.put(STATUS_DB, getStatus());
    databaseContent.put(VERSION_NUM_DB, getVersionNum());
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    String result = "logId=%1$d, annotationType=%2$s, annotationId=%3$d, firstAnnotationId=%4$d, parentId=%5$d, sequence=%6$d, languageISO=%7$s, firstAccountId=%8$d, annotationMD5=%9$s";

    return String.format(result, logId, annotationType, annotationId,
        firstAnnotationId, parentId, sequence, languageISO, firstAccountId,
        annotationMD5);
  }

}
