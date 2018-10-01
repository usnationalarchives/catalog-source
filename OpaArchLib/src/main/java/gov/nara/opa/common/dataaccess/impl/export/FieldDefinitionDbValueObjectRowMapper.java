package gov.nara.opa.common.dataaccess.impl.export;

import gov.nara.opa.common.valueobject.export.FieldDefinitionDbValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class FieldDefinitionDbValueObjectRowMapper implements
    RowMapper<FieldDefinitionDbValueObject> {

  @Override
  public FieldDefinitionDbValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    FieldDefinitionDbValueObjectExtractor extractor = new FieldDefinitionDbValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
