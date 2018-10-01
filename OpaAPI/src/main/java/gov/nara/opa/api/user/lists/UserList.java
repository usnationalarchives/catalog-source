package gov.nara.opa.api.user.lists;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

/**
 * Class UserList with the required fields and columns as the table on the
 * database.
 */
public class UserList {
  private int listId;
  private String listName;
  private Timestamp createdTs;
  private Timestamp lastModifiedTs;
  private int accountId;
  private int total;

  public UserList() {
  }

  // Non-default constructor
  public UserList(String listName, int accountId) {
    this.listName = listName;
    this.accountId = accountId;
  }

  public int getAccountId() {
    return accountId;
  }

  public Timestamp getCreatedTs() {
    return createdTs;
  }

  /**
   * @return the lastModifiedTs
   */
  public Timestamp getLastModifiedTs() {
    return lastModifiedTs;
  }

  public int getListId() {
    return listId;
  }

  public String getListName() {
    return listName;
  }

  /**
   * Get the hashMap with the values to be returned on the response.
   * 
   * @param userList
   *          The persisted list from which we obtain the information we require
   *          for the response
   * @return A LinkedHashMap with the specific values that the response require.
   */
  public LinkedHashMap<String, Object> getResponseValues(UserList userList) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
    result.put("@name", userList.getListName());
    result.put("@total", 0);
    result.put("@lastModified", userList.getCreatedTs());
    result.put("@created", userList.getCreatedTs());
    return result;
  }

  /**
   * @return the total
   */
  public int getTotal() {
    return total;
  }

  public void setAccountId(int accountId) {
    this.accountId = accountId;
  }

  public void setCreatedTs(Timestamp createdTs) {
    this.createdTs = createdTs;
  }

  /**
   * @param lastModifiedTs
   *          the lastModifiedTs to set
   */
  public void setLastModifiedTs(Timestamp lastModifiedTs) {
    this.lastModifiedTs = lastModifiedTs;
  }

  public void setListId(int listId) {
    this.listId = listId;
  }

  public void setListName(String listName) {
    this.listName = listName;
  }

  /**
   * @param total
   *          the total to set
   */
  public void setTotal(int total) {
    this.total = total;
  }

}
