package gov.nara.opa.api.dataaccess.impl.user.logs;

import gov.nara.opa.api.dataaccess.user.logs.AccountLogDao;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObject;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObjectConstants;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.web.valueobject.ValueObjectUtils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AccountLogJdbcTemplate extends AbstractOpaDbJDBCTemplate implements
    AccountLogDao, AccountLogValueObjectConstants {

  private static final String SELECT_ACCOUNT_NOTES_SQL = "SELECT L.*, AR.REASON, A.ACCOUNT_NOTE_FLAG, A.ACCOUNT_STATUS, AD.USER_NAME AS ADMIN_USER_NAME, AD.FULL_NAME AS ADMIN_FULL_NAME FROM accounts_log L LEFT OUTER JOIN accounts AD ON (L.ADMIN_ACCOUNT_ID = AD.ACCOUNT_ID), accounts_reasons AR, accounts A "
      + "WHERE L.ACCOUNT_ID = A.ACCOUNT_ID AND A.ACCOUNT_NOTE_FLAG = true AND L.REASON_ID = AR.REASON_ID AND A.USER_NAME = ? ORDER BY LOG_ID DESC";
  private static final String INSERT_ACCOUNT_LOG_SQL;
  static {
    List<String> ignoreFields = new ArrayList<String>();
    ignoreFields.add(LOG_ID_DB);
    INSERT_ACCOUNT_LOG_SQL = ValueObjectUtils.createInsertStatement(
        "INSERT INTO accounts_log", AccountLogValueObjectConstants.class,
        ignoreFields);
  }

  @Override
  public void create(AccountLogValueObject log) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    getNamedJdbcTemplate().update(INSERT_ACCOUNT_LOG_SQL,
        new MapSqlParameterSource(log.getDatabaseContent()), keyHolder);
    int logId = keyHolder.getKey().intValue();
    log.setLogId(logId);
  }

  @Override
  public List<AccountLogValueObject> getAccountNotes(String userName) {
    return getJdbcTemplate().query(SELECT_ACCOUNT_NOTES_SQL,
        new Object[] { userName }, new AccountLogValueObjectRowMapper());
  }
}
