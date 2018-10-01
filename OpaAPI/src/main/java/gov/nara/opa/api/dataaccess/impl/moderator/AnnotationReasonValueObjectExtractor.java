package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.valueobject.moderator.AnnotationReasonValueObject;
import gov.nara.opa.api.valueobject.moderator.AnnotationReasonValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AnnotationReasonValueObjectExtractor implements
    ResultSetExtractor<AnnotationReasonValueObject>,
    AnnotationReasonValueObjectConstants {

  @Override
  public AnnotationReasonValueObject extractData(ResultSet rs)
      throws SQLException, DataAccessException {

    AnnotationReasonValueObject annotationReason = new AnnotationReasonValueObject();

    annotationReason.setAccountId(rs.getInt(ACCOUNT_ID_DB));
    annotationReason.setAddedTS(rs.getTimestamp(REASON_ADDED_TS_DB));
    annotationReason.setReason(rs.getString(REASON_DB));
    annotationReason.setReasonId(rs.getInt(REASON_ID_DB));
    annotationReason.setStatus(rs.getBoolean(REASON_STATUS_DB));
    return annotationReason;
  }
}
