package gov.nara.opa.common.dataaccess.impl.export;

import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportStatusValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AccountExportStatusValueObjectExtractor implements
    ResultSetExtractor<AccountExportStatusValueObject>,
    AccountExportValueObjectConstants {

  @Override
  public AccountExportStatusValueObject extractData(ResultSet rs)
      throws SQLException {
    AccountExportStatusValueObject row = new AccountExportStatusValueObject();

    row.setRequestStatus(AccountExportStatusEnum.fromString(rs
        .getString(REQUEST_STATUS_DB)));
    row.setErrorMessage(rs.getString(ERROR_MESSAGE_DB));
    row.setTotalRecordsProcessed(rs.getInt(TOTAL_RECS_PROCESSED_DB));
    row.setTotalRecordsToBeProcessed(rs.getInt(TOTAL_RECS_TO_BE_PROCESSED_DB));
    row.setUrl(rs.getString(URL_DB));
    row.setAccountId(rs.getInt(ACCOUNT_ID_DB));
    return row;
  }

}
