package gov.nara.opa.api.dataaccess.impl.search;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AnnotationExistsRowMapper implements RowMapper<Integer> {

  @Override
  public Integer mapRow(ResultSet rs, int line) throws SQLException {
    AnnotationExistsResultSetExtractor extractor = new AnnotationExistsResultSetExtractor();
    return extractor.extractData(rs);
  }
}
