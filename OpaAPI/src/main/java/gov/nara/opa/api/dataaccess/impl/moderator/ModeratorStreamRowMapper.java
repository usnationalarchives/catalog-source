package gov.nara.opa.api.dataaccess.impl.moderator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

public class ModeratorStreamRowMapper implements
    RowMapper<Map<String, Object>> {

  @Override
  public Map<String, Object> mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    ModeratorStreamExtractor extractor = new ModeratorStreamExtractor();
    return extractor.extractData(resultSet);
  }

}
