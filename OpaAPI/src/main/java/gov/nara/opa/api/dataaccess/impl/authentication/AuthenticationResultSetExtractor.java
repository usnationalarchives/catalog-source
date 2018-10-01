package gov.nara.opa.api.dataaccess.impl.authentication;

import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class AuthenticationResultSetExtractor implements
    ResultSetExtractor<UserAccount> {

  private static OpaLogger logger = OpaLogger
      .getLogger(AuthenticationResultSetExtractor.class);

  @Override
  public UserAccount extractData(ResultSet rs) throws SQLException {
    UserAccount userAccount = new UserAccount();

    try {
      userAccount.setAccountId(rs.getInt(1));
      userAccount.setAccountType(new String(rs.getBytes(2), "UTF-8"));
      userAccount.setAccountRights(new String(rs.getBytes(3), "UTF-8"));
      userAccount.setUserName(new String(rs.getBytes(4), "UTF-8"));
      userAccount.setFullName(rs.getString(5));
      userAccount.setDisplayNameFlag(rs.getBoolean(6));
      userAccount.setEmailAddress(rs.getString(7));
      userAccount.setNaraStaff(rs.getBoolean(8));
      userAccount.setpWord(new String(rs.getBytes(9), "UTF-8"));
      userAccount.setAccountStatus(rs.getInt(10));
      userAccount.setLockedOn(rs.getTimestamp(11));
      userAccount.setLoginAttempts(rs.getInt(12));
      userAccount.setFirstInvalidLogin(rs.getTimestamp(13));
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
    }

    return userAccount;
  }

}
