package gov.nara.opa.api.dataaccess.impl.user.lists;

import gov.nara.opa.api.dataaccess.user.lists.ListDao;
import gov.nara.opa.api.valueobject.user.lists.UserListItemValueObject;
import gov.nara.opa.api.valueobject.user.lists.UserListValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ListJDBCTemplate extends AbstractOpaDbJDBCTemplate implements
    ListDao {

  public static final String SELECT_LIST_BY_NAME = "SELECT * FROM accounts_lists WHERE LIST_NAME = ? AND ACCOUNT_ID = ?";

  public static final String SELECT_LIST_ITEMS_BY_LIST_ID = "SELECT * FROM accounts_lists_items WHERE LIST_ID = ?";

  @Override
  public UserListValueObject getList(String name, Integer accountId) {
    List<UserListValueObject> list = getJdbcTemplate().query(
        SELECT_LIST_BY_NAME, new Object[] { name, accountId },
        new UserListValueObjectRowMapper());
    if (list == null || list.size() == 0) {
      return null;
    }
    return list.get(0);
  }

  @Override
  public List<UserListItemValueObject> getListItems(Integer listId) {
    return getJdbcTemplate().query(SELECT_LIST_ITEMS_BY_LIST_ID,
        new Object[] { listId }, new UserListItemValueObjectRowMapper());
  }

}
