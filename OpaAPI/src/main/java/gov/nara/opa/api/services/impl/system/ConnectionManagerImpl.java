package gov.nara.opa.api.services.impl.system;

import gov.nara.opa.api.services.system.ConnectionManager;
import gov.nara.opa.architecture.logging.OpaLogger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.springframework.stereotype.Component;

@Component
public class ConnectionManagerImpl implements ConnectionManager {
  
  private static OpaLogger logger = OpaLogger.getLogger(ConnectionManagerImpl.class);

  @Override
  public DataSource getDataSource() {
    Context ctx;
    DataSource ds = null;
    try {
      ctx = new InitialContext();

      // get the datasource jdbc/REDSTONE
      ds = (DataSource) ctx.lookup("java:jboss/datasources/OpaDB");

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }
    return ds;
  }

}
