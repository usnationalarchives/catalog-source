package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.moderator.AnnotationReason;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AnnotationReasonRowMapper implements RowMapper<AnnotationReason> {

  @Override
  public AnnotationReason mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    AnnotationReasonExtractor extractor = new AnnotationReasonExtractor();
    return extractor.extractData(resultSet);
  }

}
