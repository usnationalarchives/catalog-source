package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.valueobject.user.lists.UserListValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class UserListValueObjectRowMapper implements
    RowMapper<UserListValueObject> {

  @Override
  public UserListValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    UserListValueObjectExtractor extractor = new UserListValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
