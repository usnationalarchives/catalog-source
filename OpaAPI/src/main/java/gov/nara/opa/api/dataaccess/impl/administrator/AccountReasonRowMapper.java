package gov.nara.opa.api.dataaccess.impl.administrator;

import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AccountReasonRowMapper implements
    RowMapper<AccountReasonValueObject> {

  @Override
  public AccountReasonValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    AccountReasonExtractor extractor = new AccountReasonExtractor();
    return extractor.extractData(resultSet);
  }

}
