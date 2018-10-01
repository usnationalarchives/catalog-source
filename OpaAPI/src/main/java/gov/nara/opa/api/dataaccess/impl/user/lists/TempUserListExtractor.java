package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class TempUserListExtractor implements ResultSetExtractor<UserList> {

  private static OpaLogger logger = OpaLogger
      .getLogger(TempUserListExtractor.class);

  @Override
  public UserList extractData(ResultSet resultSet) throws SQLException,
      DataAccessException {
    UserList userList = new UserList();

    try {
      userList.setAccountId(resultSet.getInt("account_id"));
      userList
          .setListName(new String(resultSet.getBytes("list_name"), "UTF-8"));
      userList.setCreatedTs(resultSet.getTimestamp("list_ts"));
      userList.setListId(resultSet.getInt("list_id"));
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    return userList;
  }

}
