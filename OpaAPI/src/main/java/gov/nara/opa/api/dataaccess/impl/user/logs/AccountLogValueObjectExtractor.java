package gov.nara.opa.api.dataaccess.impl.user.logs;

import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObjectConstants;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObject;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObjectConstants;
import gov.nara.opa.architecture.dataaccess.ObjectExtractorHelper;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AccountLogValueObjectExtractor implements
    ResultSetExtractor<AccountLogValueObject>, AccountLogValueObjectConstants,
    UserAccountValueObjectConstants, AccountReasonValueObjectConstants {

  @Override
  public AccountLogValueObject extractData(ResultSet rs) throws SQLException,
      DataAccessException {
    AccountLogValueObject log = new AccountLogValueObject();

    log.setAccountId(rs.getInt(AccountLogValueObjectConstants.ACCOUNT_ID_DB));
    log.setAction(rs.getString(ACTION_DB));
    log.setAdminAccountId(ObjectExtractorHelper.getIntIfItExists(rs,
        ADMIN_ACCOUNT_ID_DB));

    log.setAdminFullName(ObjectExtractorHelper.getStringIfItExists(rs,
        ADMIN_FULL_NAME_DERIVED));

    log.setAdminUserName(ObjectExtractorHelper.getStringIfItExists(rs,
        ADMIN_USER_NAME_DERIVED));
    log.setLogId(rs.getInt(LOG_ID_DB));
    log.setLogTs(rs.getTimestamp(LOG_TS_DB));
    log.setNotes(rs.getString(NOTES_DB));
    log.setReasonId(rs.getInt(AccountLogValueObjectConstants.REASON_ID_DB));
    log.setStatus(rs.getBoolean(STATUS_DB));

    log.setReason(ObjectExtractorHelper.getStringIfItExists(rs, REASON_DB));
    log.setAccountStatus(ObjectExtractorHelper.getBooleanIfItExists(rs,
        ACCOUNT_STATUS_DB));
    log.setAccountHasNotes(ObjectExtractorHelper.getBooleanIfItExists(rs,
        ACCOUNT_NOTE_FLAG_DB));

    return log;
  }
}
