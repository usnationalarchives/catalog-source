package gov.nara.opa.api.dataaccess.impl.search;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class AnnotationExistsResultSetExtractor implements
    ResultSetExtractor<Integer> {

  @Override
  public Integer extractData(ResultSet resultSet) throws SQLException {

    return resultSet.getInt("log_id");
  }

}
