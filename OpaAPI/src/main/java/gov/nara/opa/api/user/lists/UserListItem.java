package gov.nara.opa.api.user.lists;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

/**
 * Class UserList with the required fields and columns as the table on the
 * database.
 */
public class UserListItem {
  private int userListItemId;
  private int listId;
  private String naId;
  private String objectId;
  private String opaId;
  private Timestamp itemTs;

  public UserListItem() {
  }

  /**
   * Non-default constructor
   */
  public UserListItem(int listId, String opaId) {
    setListId(listId);
    setOpaId(opaId);
  }

  /**
   * @return the itemTs
   */
  public Timestamp getItemTs() {
    return itemTs;
  }

  public int getListId() {
    return listId;
  }

  /**
   * @return the naId
   */
  public String getNaId() {
    return naId;
  }

  /**
   * @return the objectId
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * @return the opaId
   */
  public String getOpaId() {
    return opaId;
  }

  /**
   * Get the hashMap with the values to be returned on the response.
   * 
   * @param userList
   *          The persisted list from which we obtain the information we require
   *          for the response
   * @return A LinkedHashMap with the specific values that the response require.
   */
  public LinkedHashMap<String, Object> getResponseValues(UserListItem item) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
    result.put("@listItemId", item.getUserListItemId());
    result.put("@listId", item.getListId());
    result.put("@opaId", item.getOpaId());
    result.put("@created", item.getItemTs());
    return result;
  }

  /**
   * @return the userListItemId
   */
  public int getUserListItemId() {
    return userListItemId;
  }

  /**
   * @param itemTs
   *          the itemTs to set
   */
  public void setItemTs(Timestamp itemTs) {
    this.itemTs = itemTs;
  }

  public void setListId(int listId) {
    this.listId = listId;
  }

  /**
   * @param naId
   *          the naId to set
   */
  public void setNaId(String naId) {
    this.naId = naId;
  }

  /**
   * @param objectId
   *          the objectId to set
   */
  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  /**
   * @param opaId
   *          the opaId to set
   */
  public void setOpaId(String opaId) {
    this.opaId = opaId;
  }

  /**
   * @param userListItemId
   *          the userListItemId to set
   */
  public void setUserListItemId(int userListItemId) {
    this.userListItemId = userListItemId;
  }

}
