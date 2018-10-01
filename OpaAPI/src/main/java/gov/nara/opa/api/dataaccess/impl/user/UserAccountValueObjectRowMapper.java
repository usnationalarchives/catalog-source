package gov.nara.opa.api.dataaccess.impl.user;

import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Row mapper implementation for UserAccount
 */
public class UserAccountValueObjectRowMapper implements
    RowMapper<UserAccountValueObject> {
  @Override
  public UserAccountValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    UserAccountValueObjectExtractor extractor = new UserAccountValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
