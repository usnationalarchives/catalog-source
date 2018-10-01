package gov.nara.opa.api.dataaccess.impl.annotation.locks;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AnnotationLockExtractor implements
    ResultSetExtractor<AnnotationLock> {
  
  private static OpaLogger logger = OpaLogger.getLogger(AnnotationLockExtractor.class);

  @Override
  public AnnotationLock extractData(ResultSet resultSet) throws SQLException,
      DataAccessException {
    AnnotationLock lock = new AnnotationLock();

    try {
      lock.setLockId(resultSet.getInt("lock_id"));
      lock.setAccountId(resultSet.getInt("account_id"));
      lock.setNaId(new String(resultSet.getBytes("na_id"), "UTF-8"));
      lock.setObjectId(new String(resultSet.getBytes("object_id"), "UTF-8"));
      if (resultSet.getString("opa_id") != null) {
        lock.setOpaId(new String(resultSet.getBytes("opa_id"), "UTF-8"));
      }
      if (resultSet.getString("language_iso") != null) {
        lock.setLanguageISO(new String(resultSet.getBytes("language_iso"),
            "UTF-8"));
      }
      lock.setLockTS(resultSet.getTimestamp("lock_ts"));
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return lock;
  }

}
