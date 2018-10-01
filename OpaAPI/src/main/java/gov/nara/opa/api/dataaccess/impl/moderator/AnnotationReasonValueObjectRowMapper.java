package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.valueobject.moderator.AnnotationReasonValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AnnotationReasonValueObjectRowMapper implements
    RowMapper<AnnotationReasonValueObject> {

  @Override
  public AnnotationReasonValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    AnnotationReasonValueObjectExtractor extractor = new AnnotationReasonValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
