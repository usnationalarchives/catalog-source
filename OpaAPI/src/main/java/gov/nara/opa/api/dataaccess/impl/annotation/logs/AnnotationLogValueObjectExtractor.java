package gov.nara.opa.api.dataaccess.impl.annotation.logs;


import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class AnnotationLogValueObjectExtractor implements
    ResultSetExtractor<AnnotationLogValueObject>,
    AnnotationLogValueObjectConstants {

  @Override
  public AnnotationLogValueObject extractData(ResultSet rs)
      throws SQLException, DataAccessException {
    AnnotationLogValueObject log = new AnnotationLogValueObject();

    log.setAccountId(rs.getInt(ACCOUNT_ID_DB));
    log.setAction(rs.getString(ACTION_DB));
    log.setAffectsAccountId(rs.getInt(AFFECTS_ACCOUNT_ID_DB));
    log.setAnnotationId(rs.getInt(ANNOTATION_ID_DB));
    log.setAnnotationMD5(rs.getString(ANNOTATION_MD5_DB));
    log.setAnnotationType(rs.getString(ANNOTATION_TYPE_DB));
    log.setFirstAccountId(rs.getInt(FIRST_ACCOUNT_ID_DB));
    log.setFirstAnnotationId(rs.getInt(FIRST_ANNOTATION_ID_DB));
    log.setLanguageISO(rs.getString(LANGUAGE_ISO_DB));
    log.setLogId(rs.getInt(LOG_ID_DB));
    log.setLogTS(rs.getTimestamp(LOG_TS_DB));
    log.setNaId(rs.getString(NA_ID_DB));
    log.setNotes(rs.getString(NOTES_DB));
    log.setObjectId(rs.getString(OBJECT_ID_DB));
    log.setOpaId(rs.getString(OPA_ID_DB));
    log.setPageNum(rs.getInt(PAGE_NUM_DB));
    log.setParentId(rs.getInt(PARENT_ID_DB));
    log.setReasonId(rs.getInt(REASON_ID_DB));
    log.setSequence(rs.getInt(SEQUENCE_DB));
    log.setSessionId(rs.getString(SESSION_ID_DB));
    log.setStatus(rs.getBoolean(STATUS_DB));
    log.setVersionNum(rs.getInt(VERSION_NUM_DB));
    return log;
  }

}
