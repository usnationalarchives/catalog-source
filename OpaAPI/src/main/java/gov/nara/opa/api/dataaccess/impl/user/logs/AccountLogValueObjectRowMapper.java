package gov.nara.opa.api.dataaccess.impl.user.logs;

import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AccountLogValueObjectRowMapper implements
    RowMapper<AccountLogValueObject> {
  @Override
  public AccountLogValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    AccountLogValueObjectExtractor extractor = new AccountLogValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
