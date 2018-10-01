package gov.nara.opa.api.dataaccess.impl.authentication;

import gov.nara.opa.api.user.accounts.UserAccount;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class AuthenticationRowMapper implements RowMapper<UserAccount> {

  @Override
  public UserAccount mapRow(ResultSet rs, int line) throws SQLException {
    AuthenticationResultSetExtractor extractor = new AuthenticationResultSetExtractor();
    return extractor.extractData(rs);
  }

}
