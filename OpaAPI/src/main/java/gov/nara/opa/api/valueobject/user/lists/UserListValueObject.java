package gov.nara.opa.api.valueobject.user.lists;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserListValueObject extends AbstractWebEntityValueObject implements
    UserListValueObjectConstants {

  private Integer listId;
  private Integer accountId;
  private String listName;
  private Timestamp listTs;

  public Integer getListId() {
    return listId;
  }

  public void setListId(Integer listId) {
    this.listId = listId;
  }

  public Integer getAccountId() {
    return accountId;
  }

  public void setAccountId(Integer accountId) {
    this.accountId = accountId;
  }

  public String getListName() {
    return listName;
  }

  public void setListName(String listName) {
    this.listName = listName;
  }

  public Timestamp getListTs() {
    return listTs;
  }

  public void setListTs(Timestamp listTs) {
    this.listTs = listTs;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    Map<String, Object> databaseContent = new HashMap<String, Object>();
    databaseContent.put(LIST_ID_DB, getListId());
    databaseContent.put(LIST_NAME_DB, getListName());
    databaseContent.put(LIST_TS_DB, getListTs());
    databaseContent.put(ACCOUNT_ID_DB, getAccountId());
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }

}
