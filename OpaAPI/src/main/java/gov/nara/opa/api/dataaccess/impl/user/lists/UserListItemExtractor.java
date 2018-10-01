package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class UserListItemExtractor implements ResultSetExtractor<UserListItem> {
  
  private static OpaLogger logger = OpaLogger.getLogger(UserListItemExtractor.class);

  @Override
  public UserListItem extractData(ResultSet resultSet) throws SQLException,
      DataAccessException {

    UserListItem item = new UserListItem();
    try {

      item.setUserListItemId(resultSet.getInt("list_item_id"));
      item.setListId(resultSet.getInt("list_id"));
      // item.setNaId(new String(resultSet.getBytes("na_id"), "UTF-8"));
      // item.setObjectId(new String(resultSet.getBytes("object_id"),
      // "UTF-8"));
      item.setOpaId(new String(resultSet.getBytes("opa_id"), "UTF-8"));
      item.setItemTs(resultSet.getTimestamp("item_ts"));

    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return item;
  }

}
