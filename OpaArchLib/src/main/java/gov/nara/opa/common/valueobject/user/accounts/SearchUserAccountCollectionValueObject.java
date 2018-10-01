package gov.nara.opa.common.valueobject.user.accounts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SearchUserAccountCollectionValueObject extends
    UserAccountCollectionValueObject implements UserAccountValueObjectConstants {

  private UserAccountCollectionValueObject internalCollection;

  private List<SearchUserAccountValueObject> searchUsers;

  private SearchUserAccountCollectionValueObject(
      List<UserAccountValueObject> users) {
    super(users);
  }

  public SearchUserAccountCollectionValueObject(
      UserAccountCollectionValueObject userCollection, boolean showPrivateData) {
    this(userCollection.getUsers());

    internalCollection = userCollection;
    searchUsers = new ArrayList<SearchUserAccountValueObject>();

    for (UserAccountValueObject user : userCollection.getUsers()) {
      SearchUserAccountValueObject searchUser = new SearchUserAccountValueObject(
          user);
      searchUser.setShowPrivateData(showPrivateData);

      searchUsers.add(searchUser);
    }

  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    aspireContent.put(TOTAL_RECORDS_ASP, internalCollection.getTotal());
    aspireContent.put(SEARCH_TOTAL_RECORDS_ASP,
        internalCollection.getSearchTotal());
    aspireContent.put(USER_ACCOUNT_ENTITY_NAME, searchUsers);
    return aspireContent;
  }

}
