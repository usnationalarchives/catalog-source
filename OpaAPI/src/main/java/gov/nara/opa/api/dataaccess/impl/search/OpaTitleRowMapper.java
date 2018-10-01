package gov.nara.opa.api.dataaccess.impl.search;

import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class OpaTitleRowMapper implements RowMapper<OpaTitle> {

  @Override
  public OpaTitle mapRow(ResultSet rs, int line) throws SQLException {
    OpaTitleResultSetExtractor extractor = new OpaTitleResultSetExtractor();
    return extractor.extractData(rs);
  }
}
