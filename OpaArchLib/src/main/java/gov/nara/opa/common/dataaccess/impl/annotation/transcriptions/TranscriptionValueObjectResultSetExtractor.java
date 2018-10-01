package gov.nara.opa.common.dataaccess.impl.annotation.transcriptions;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;
import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObjectConstants;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class TranscriptionValueObjectResultSetExtractor implements
    ResultSetExtractor<TranscriptionValueObject>,
    TranscriptionValueObjectConstants, UserAccountValueObjectConstants {

  private static OpaLogger logger = OpaLogger
      .getLogger(TranscriptionValueObjectResultSetExtractor.class);

  @Override
  public TranscriptionValueObject extractData(ResultSet rs) throws SQLException {
    TranscriptionValueObject row = new TranscriptionValueObject();
    row.setAccountId(rs.getInt(TranscriptionValueObjectConstants.ACCOUNT_ID_DB));
    row.setAnnotation(new String(rs.getBytes(ANNOTATION_DB)));
    row.setAnnotationId(rs.getInt(ANNOTATION_ID_DB));
    row.setAnnotationMD5(rs.getString(ANNOTATION_MD5_DB));
    row.setAnnotationTS(rs.getTimestamp(ANNOTATION_TS_DB));
    row.setFirstAnnotationId(rs.getInt(FIRST_ANNOTATION_ID_DB));
    row.setNaId(rs.getString(NA_ID_DB));
    row.setObjectId(rs.getString(OBJECT_ID_DB));
    row.setOpaId(rs.getString(OPA_ID_DB));
    row.setPageNum(rs.getInt(PAGE_NUM_DB));
    row.setSavedVersionNumber(rs.getInt(SAVED_VERS_NUM_DB));
    row.setStatus(rs.getBoolean(STATUS_DB));
    UserAccountValueObject user = new UserAccountValueObject();
    user.setAccountId(rs.getInt(UserAccountValueObjectConstants.ACCOUNT_ID_DB));
    user.setUserName(rs.getString(USER_NAME_DB));
    user.setDisplayFullName(rs.getBoolean(DISPLAY_NAME_FLAG_DB));
    user.setNaraStaff(rs.getBoolean(IS_NARA_STAFF_DB));
    user.setFullName(rs.getString(FULL_NAME_DB));
    row.setUser(user);
    return row;
  }
}
