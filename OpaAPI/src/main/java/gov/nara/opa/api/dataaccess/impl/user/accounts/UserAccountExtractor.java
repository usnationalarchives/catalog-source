package gov.nara.opa.api.dataaccess.impl.user.accounts;

import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

/**
 * ResultSetExtractor implementation for User Account
 */
@Component
public class UserAccountExtractor implements ResultSetExtractor<UserAccount> {

  private static OpaLogger logger = OpaLogger
      .getLogger(UserAccountExtractor.class);

  @Override
  public UserAccount extractData(ResultSet resultSet) throws SQLException,
      DataAccessException {
    UserAccount userAccount = new UserAccount();

    try {
      userAccount.setAccountId(resultSet.getInt("account_id"));
      userAccount.setUserName(new String(resultSet.getBytes("user_name"),
          "UTF-8"));
      userAccount.setAccountType(new String(resultSet.getBytes("account_type"),
          "UTF-8"));
      if (resultSet.getString("full_name") != null) {
        userAccount.setFullName(new String(resultSet.getBytes("full_name"),
            "UTF-8"));
      }
      userAccount.setDisplayNameFlag(resultSet.getBoolean("display_name_flag"));
      userAccount.setpWord(new String(resultSet.getBytes("p_word"), "UTF-8"));
      userAccount.setEmailAddress(new String(resultSet
          .getBytes("email_address"), "UTF-8"));
      userAccount.setLastActionTS(resultSet.getTimestamp("last_action_ts"));
      userAccount.setAccountNoteFlag(resultSet.getBoolean("account_note_flag"));
      userAccount.setNaraStaff(resultSet.getBoolean("is_nara_staff"));
      userAccount.setAccountCreatedTS(resultSet
          .getTimestamp("account_created_ts"));
      userAccount.setAccountRights(new String(resultSet
          .getBytes("account_rights"), "UTF-8"));
      userAccount.setAccountStatus(resultSet.getInt("account_status"));
      userAccount.setpWordChangeTS(resultSet.getTimestamp("p_word_change_ts"));
      userAccount.setVerificationGuid(resultSet.getString("verification_guid"));
      userAccount.setVerificationTS(resultSet.getTimestamp("verification_ts"));
      userAccount.setpWordChangeId(resultSet.getInt("p_word_change_id"));
      userAccount.setLoginAttempts(resultSet.getInt("login_attempts"));
      userAccount.setLockedOn(resultSet.getTimestamp("locked_on"));
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return userAccount;
  }

}
