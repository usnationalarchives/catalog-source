package gov.nara.opa.api.dataaccess.impl.annotation.logs;

import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AnnotationLogValueObjectRowMapper implements
    RowMapper<AnnotationLogValueObject> {

  @Override
  public AnnotationLogValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    AnnotationLogValueObjectExtractor extractor = new AnnotationLogValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
