package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.valueobject.user.lists.UserListItemValueObject;
import gov.nara.opa.api.valueobject.user.lists.UserListItemValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class UserListItemValueObjectExtractor implements
    ResultSetExtractor<UserListItemValueObject>,
    UserListItemValueObjectConstants {

  @Override
  public UserListItemValueObject extractData(ResultSet rs) throws SQLException,
      DataAccessException {

    UserListItemValueObject row = new UserListItemValueObject();
    row.setItemTs(rs.getTimestamp(ITEM_TS_DB));
    row.setListId(rs.getInt(LIST_ID_DB));
    row.setListItemId(rs.getInt(LIST_ITEM_ID_DB));
    row.setNaId(rs.getString(NA_ID_DB));
    row.setObjectId(rs.getString(OBJECT_ID_DB));
    row.setOpaId(rs.getString(OPA_ID_DB));
    return row;
  }

}
