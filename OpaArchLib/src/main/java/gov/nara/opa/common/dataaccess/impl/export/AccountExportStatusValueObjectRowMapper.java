package gov.nara.opa.common.dataaccess.impl.export;

import gov.nara.opa.common.valueobject.export.AccountExportStatusValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AccountExportStatusValueObjectRowMapper implements
    RowMapper<AccountExportStatusValueObject> {

  @Override
  public AccountExportStatusValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    AccountExportStatusValueObjectExtractor extractor = new AccountExportStatusValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
