package gov.nara.opa.api.services.impl.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import org.springframework.jdbc.core.RowMapper;


public class TagMigrationRowMapper implements RowMapper<LinkedHashMap<String,Object>> {

  @Override
  public LinkedHashMap<String,Object> mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    TagsMigrationExtractor extractor = new TagsMigrationExtractor();
    return extractor.extractData(resultSet);
  }

}
