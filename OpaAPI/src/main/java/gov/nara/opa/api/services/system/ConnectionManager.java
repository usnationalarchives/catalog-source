package gov.nara.opa.api.services.system;

import javax.sql.DataSource;

public interface ConnectionManager {
  public DataSource getDataSource();
}
