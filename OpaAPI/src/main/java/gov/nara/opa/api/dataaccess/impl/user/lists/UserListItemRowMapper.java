package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.user.lists.UserListItem;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class UserListItemRowMapper implements RowMapper<UserListItem> {

  @Override
  public UserListItem mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    UserListItemExtractor extractor = new UserListItemExtractor();
    return extractor.extractData(resultSet);
  }

}
