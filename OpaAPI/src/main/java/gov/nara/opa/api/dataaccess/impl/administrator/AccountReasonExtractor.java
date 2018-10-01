package gov.nara.opa.api.dataaccess.impl.administrator;

import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AccountReasonExtractor implements
    ResultSetExtractor<AccountReasonValueObject>,
    AccountReasonValueObjectConstants {

  @Override
  public AccountReasonValueObject extractData(ResultSet rs) throws SQLException {
    AccountReasonValueObject accountReason = new AccountReasonValueObject();
    accountReason.setAccountId(rs.getInt(ACCOUNT_ID_DB));
    accountReason.setReason(rs.getString(REASON_DB));
    accountReason.setReasonAddedTs(rs.getTimestamp(REASON_ADDED_TS_DB));
    accountReason.setReasonId(rs.getInt(REASON_ID_DB));
    accountReason.setReasonStatus(rs.getBoolean(REASON_STATUS_DB));
    return accountReason;
  }

}
