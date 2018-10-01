package gov.nara.opa.common.valueobject.user.accounts;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserAccountCollectionValueObject extends
    AbstractWebEntityValueObject implements UserAccountValueObjectConstants {

  private List<UserAccountValueObject> users;
  private int total;
  private int searchTotal;

  public static final String USER_ACCOUNT_ENTITY_NAME = "user";

  protected List<UserAccountValueObject> getUsers() {
    return users;
  }

  protected void setUsers(List<UserAccountValueObject> users) {
    this.users = users;
  }

  protected int getTotal() {
    return total;
  }

  protected void setTotal(int total) {
    this.total = total;
  }

  protected int getSearchTotal() {
    return searchTotal;
  }

  protected void setSearchTotal(int searchTotal) {
    this.searchTotal = searchTotal;
  }

  public UserAccountCollectionValueObject(List<UserAccountValueObject> users) {
    this.users = users;
    this.total = users != null ? users.size() : 0;
  }
  
  public UserAccountCollectionValueObject(List<UserAccountValueObject> users, int searchTotal) {
    this(users);
    this.searchTotal = searchTotal;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    aspireContent.put(TOTAL_RECORDS_ASP, total);
    aspireContent.put(SEARCH_TOTAL_RECORDS_ASP, searchTotal);
    aspireContent.put(USER_ACCOUNT_ENTITY_NAME, users);
    return aspireContent;
  }

}
