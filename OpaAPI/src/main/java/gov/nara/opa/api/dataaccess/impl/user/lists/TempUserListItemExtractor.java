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
public class TempUserListItemExtractor implements
    ResultSetExtractor<UserListItem> {

  private static OpaLogger logger = OpaLogger
      .getLogger(TempUserListItemExtractor.class);

  @Override
  public UserListItem extractData(ResultSet resultSet) throws SQLException,
      DataAccessException {

    UserListItem item = new UserListItem();
    try {

      item.setUserListItemId(resultSet.getInt("list_item_id"));
      item.setListId(resultSet.getInt("list_id"));
      if (resultSet.getString("na_id") != null)
        item.setNaId(new String(resultSet.getBytes("na_id"), "UTF-8"));
      if (resultSet.getString("object_id") != null)
        item.setObjectId(new String(resultSet.getBytes("object_id"), "UTF-8"));
      if (resultSet.getString("opa_id") != null)
        item.setOpaId(new String(resultSet.getBytes("opa_id"), "UTF-8"));
      if (resultSet.getString("item_ts") != null)
        item.setItemTs(resultSet.getTimestamp("item_ts"));

    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    return item;
  }

}
