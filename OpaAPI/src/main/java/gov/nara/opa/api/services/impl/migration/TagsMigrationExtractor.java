package gov.nara.opa.api.services.impl.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class TagsMigrationExtractor implements ResultSetExtractor<LinkedHashMap<String,Object>> {

  @Override
  public LinkedHashMap<String, Object> extractData(ResultSet rs)
      throws SQLException, DataAccessException {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
    
    result.put("annotationId", rs.getString("annotation_id"));
    result.put("naId", rs.getString("na_id"));
    result.put("objectId", rs.getString("object_id"));
    
    return result;
  }

}
