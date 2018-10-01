package gov.nara.opa.common.dataaccess.impl.annotation.tags;

import gov.nara.opa.common.valueobject.annotation.tags.Tag;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TagRowMapper implements RowMapper<Tag> {

  @Override
  public Tag mapRow(ResultSet rs, int line) throws SQLException {
    TagResultSetExtractor extractor = new TagResultSetExtractor();
    return extractor.extractData(rs);
  }
}
