package gov.nara.opa.api.dataaccess.impl.user.accounts;

import gov.nara.opa.api.user.accounts.UserAccount;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Row mapper implementation for UserAccount
 */
public class UserAccountRowMapper implements RowMapper<UserAccount> {
  @Override
  public UserAccount mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    UserAccountExtractor extractor = new UserAccountExtractor();
    return extractor.extractData(resultSet);
  }

}
