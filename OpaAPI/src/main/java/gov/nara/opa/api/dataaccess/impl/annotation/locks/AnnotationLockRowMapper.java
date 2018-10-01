package gov.nara.opa.api.dataaccess.impl.annotation.locks;

import java.sql.ResultSet;
import java.sql.SQLException;
import gov.nara.opa.api.annotation.locks.AnnotationLock;
import org.springframework.jdbc.core.RowMapper;

public class AnnotationLockRowMapper implements RowMapper<AnnotationLock> {

  @Override
  public AnnotationLock mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    AnnotationLockExtractor extractor = new AnnotationLockExtractor();
    return extractor.extractData(resultSet);
  }

}
