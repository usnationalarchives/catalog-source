package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.user.lists.UserList;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TempUserListRowMapper implements RowMapper<UserList> {

  @Override
  public UserList mapRow(ResultSet resultSet, int rowNum) throws SQLException {
    TempUserListExtractor extractor = new TempUserListExtractor();
    return extractor.extractData(resultSet);
  }

}
