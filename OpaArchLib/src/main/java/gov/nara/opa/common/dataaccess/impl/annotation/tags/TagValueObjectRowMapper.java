package gov.nara.opa.common.dataaccess.impl.annotation.tags;

import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TagValueObjectRowMapper implements RowMapper<TagValueObject> {

  @Override
  public TagValueObject mapRow(ResultSet rs, int line) throws SQLException {
    TagValueOjectResultSetExtractor extractor = new TagValueOjectResultSetExtractor();
    return extractor.extractData(rs);
  }
}
