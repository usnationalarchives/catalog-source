package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.valueobject.user.lists.UserListItemValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class UserListItemValueObjectRowMapper implements
    RowMapper<UserListItemValueObject> {

  @Override
  public UserListItemValueObject mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    UserListItemValueObjectExtractor extractor = new UserListItemValueObjectExtractor();
    return extractor.extractData(resultSet);
  }

}
