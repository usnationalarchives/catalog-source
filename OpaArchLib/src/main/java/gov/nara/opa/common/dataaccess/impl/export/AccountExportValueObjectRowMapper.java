package gov.nara.opa.common.dataaccess.impl.export;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AccountExportValueObjectRowMapper implements
    RowMapper<AccountExportValueObject> {

  @Override
  public AccountExportValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    AccountExportValueObjectExtractor extractor = new AccountExportValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
