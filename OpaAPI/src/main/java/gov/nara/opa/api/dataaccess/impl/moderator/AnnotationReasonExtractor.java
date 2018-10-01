package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.moderator.AnnotationReason;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AnnotationReasonExtractor implements
    ResultSetExtractor<AnnotationReason> {

  @Override
  public AnnotationReason extractData(ResultSet resultSet) throws SQLException,
      DataAccessException {

    AnnotationReason annotationReason = new AnnotationReason();

    annotationReason.setReasonId(resultSet.getInt("reason_id"));
    annotationReason.setStatus(resultSet.getInt("reason_status"));
    annotationReason.setAccountId(resultSet.getInt("account_id"));
    annotationReason.setReason(resultSet.getString("reason"));
    annotationReason.setAddedTs(resultSet.getTimestamp("reason_added_ts"));

    return annotationReason;
  }

}
