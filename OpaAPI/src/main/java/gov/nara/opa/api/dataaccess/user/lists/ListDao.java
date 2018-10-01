package gov.nara.opa.api.dataaccess.user.lists;

import gov.nara.opa.api.valueobject.user.lists.UserListItemValueObject;
import gov.nara.opa.api.valueobject.user.lists.UserListValueObject;

import java.util.List;

public interface ListDao {

  UserListValueObject getList(String name, Integer accountId);

  List<UserListItemValueObject> getListItems(Integer listId);
}
