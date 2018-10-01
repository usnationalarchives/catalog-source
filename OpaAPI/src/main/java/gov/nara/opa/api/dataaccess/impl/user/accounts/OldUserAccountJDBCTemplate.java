package gov.nara.opa.api.dataaccess.impl.user.accounts;

import gov.nara.opa.api.dataaccess.user.accounts.OldUserAccountDao;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBCTemplate implementation for User Account
 */
@Component
@Transactional
public class OldUserAccountJDBCTemplate extends AbstractOpaDbJDBCTemplate
    implements OldUserAccountDao {

  private String selectStatement = "SELECT account_id, user_name, "
      + "account_type, full_name, display_name_flag, "
      + "p_word, email_address, account_status, "
      + "last_action_ts, is_nara_staff, "
      + "account_note_flag, account_created_ts, "
      + "account_rights, p_word_change_ts, "
      + "verification_ts, verification_guid, "
      + "p_word_change_id, locked_on, login_attempts, first_invalid_login FROM accounts ";

  @Override
  public UserAccount select(int userId) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException {

    String sql = selectStatement + "WHERE account_id = ?";

    List<UserAccount> users = getJdbcTemplate().query(sql,
        new Object[] { userId }, new UserAccountRowMapper());

    return (users != null && !users.isEmpty() ? users.get(0) : null);
  }

  @Override
  public List<UserAccount> select(LinkedHashMap<String, byte[]> parameters)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException {

    String sql = selectStatement + "WHERE " + getWhereClause(parameters);

    Object[] paramObjectArray = parameters.values().toArray();

    return getJdbcTemplate().query(sql, paramObjectArray,
        new UserAccountRowMapper());

  }

  private String getWhereClause(LinkedHashMap<String, byte[]> parameters) {
    String whereClause = "";
    for (Entry<String, byte[]> parameterEntry : parameters.entrySet()) {
      String dbColumnName = parameterEntry.getKey();

      whereClause += (whereClause.length() > 0 ? " AND " : "") + dbColumnName;
      whereClause += " = ?";

    }
    return whereClause;
  }

  @Override
  public boolean create(UserAccount userAccount) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException {
    boolean result = false;

    String sql = "INSERT INTO accounts "
        + "(user_name, full_name, account_type, email_address, p_word, "
        + "account_created_ts, last_action_ts, account_note_flag, "
        + "is_nara_staff, display_name_flag, account_rights, account_status, "
        + "p_word_change_ts, verification_ts, verification_guid) "
        + "VALUES (?, ?, ?, ?, ?, " + "now(), now(), ?, " + "?, ?, ?, ?, "
        + "now(), now(), ?)";

    Object[] parameters = new Object[] {
        userAccount.getUserName().getBytes("UTF-8"),
        (userAccount.getFullName() != null
            && !userAccount.getFullName().isEmpty() ? userAccount.getFullName()
            .getBytes("UTF-8") : null),
        userAccount.getAccountType().getBytes("UTF-8"),
        userAccount.getEmailAddress().getBytes("UTF-8"),
        userAccount.getpWord().getBytes("UTF-8"),
        userAccount.isAccountNoteFlag(), userAccount.isNaraStaff(),
        userAccount.isDisplayNameFlag(),
        userAccount.getAccountRights().getBytes("UTF-8"),
        userAccount.getAccountStatus(),
        userAccount.getVerificationGuid().getBytes("UTF-8") };

    result = (getJdbcTemplate().update(sql, parameters) > 0);

    return result;
  }

  @Override
  public boolean update(UserAccount userAccount) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException {
    boolean result = false;

    String sql = "UPDATE accounts SET " + "full_name = ?, account_type = ?, "
        + "email_address = ?, p_word = ?, " + "last_action_ts = now(), "
        + "account_note_flag = ?, is_nara_staff = ?, "
        + "display_name_flag = ?, account_rights = ?, "
        + "account_status = ?, p_word_change_ts = ?, "
        + "verification_ts = ?, verification_guid = ?, "
        + "p_word_change_id = ? " + "WHERE account_id = ?";

    Object[] parameters = new Object[] {
        (userAccount.getFullName() != null ? userAccount.getFullName()
            .getBytes("UTF-8") : null),
        userAccount.getAccountType().getBytes("UTF-8"),
        userAccount.getEmailAddress().getBytes("UTF-8"),
        userAccount.getpWord().getBytes("UTF-8"),
        userAccount.isAccountNoteFlag(), userAccount.isNaraStaff(),
        userAccount.isDisplayNameFlag(),
        userAccount.getAccountRights().getBytes("UTF-8"),
        userAccount.getAccountStatus(), userAccount.getpWordChangeTS(),
        userAccount.getVerificationTS(), userAccount.getVerificationGuid(),
        userAccount.getpWordChangeId(), userAccount.getAccountId() };

    result = (getJdbcTemplate().update(sql, parameters) > 0);

    return result;
  }

}
