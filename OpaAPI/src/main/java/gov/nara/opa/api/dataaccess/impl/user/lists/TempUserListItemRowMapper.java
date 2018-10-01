package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.user.lists.UserListItem;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TempUserListItemRowMapper implements RowMapper<UserListItem> {

  @Override
  public UserListItem mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    TempUserListItemExtractor extractor = new TempUserListItemExtractor();
    return extractor.extractData(resultSet);
  }

}
