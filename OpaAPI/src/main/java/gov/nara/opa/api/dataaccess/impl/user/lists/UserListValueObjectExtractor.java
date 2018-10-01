package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.valueobject.user.lists.UserListValueObject;
import gov.nara.opa.api.valueobject.user.lists.UserListValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class UserListValueObjectExtractor implements
    ResultSetExtractor<UserListValueObject>, UserListValueObjectConstants {

  @Override
  public UserListValueObject extractData(ResultSet rs) throws SQLException,
      DataAccessException {
    UserListValueObject row = new UserListValueObject();
    row.setAccountId(rs.getInt(ACCOUNT_ID_DB));
    row.setListId(rs.getInt(LIST_ID_DB));
    row.setListName(rs.getString(LIST_NAME_DB));
    row.setListTs(rs.getTimestamp(LIST_TS_DB));
    return row;
  }

}
