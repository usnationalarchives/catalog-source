/**
 * 
 */
package gov.nara.opa.api.services.user.lists;

import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.api.valueobject.user.lists.UserListCollectionValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;

public interface ViewUserListService {

  /**
   * Retrieve all lists for this accountId
   * 
   * @param accountId
   *          The accountId we are filtering by.
   * @param offset
   *          The offset specifies the offset of the first row to return
   * @param rows
   *          The count specifies maximum number of rows to return
   * @return Collection of lists for the specified accountId
   */
  public UserListCollectionValueObject viewMyLists(int accountId, int offset, int rows);

  /**
   * Retrieve all lists for this accountId
   * 
   * @param accountId
   *          The accountId we are filtering by.
   * @return Collection of lists for the specified accountId
   */
  public List<UserList> viewMyLists(int accountId);

  /**
   * 
   * Select and return the list from the database using the parameter listname.
   * 
   * @param listName
   *          The name of the list that we want to select.
   * @return The selected list or null if not found.
   */
  public UserList getList(String listName);

  /**
   * 
   * Select and return the list from the database using the parameter listname.
   * 
   * @param listName
   *          The name of the list that we want to select.
   * @return The selected list or null if not found.
   */
  public UserList getList(String listName, int accountId);

  /**
   * Get a collection of lists items filtering by listId.
   * 
   * @param listId
   *          The list id that will be used to filter
   * @return Collection of items of the specified list
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  public List<UserListItem> getListItems(int listId);

  /**
   * Get a collection of lists items filtering by listId.
   * 
   * @param listId
   *          The list id that will be used to filter
   * @param offset
   *          The offset specifies the offset of the first row to return
   * @param rows
   *          The count specifies maximum number of rows to return
   * @return Collection of items of the specified list
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  public List<UserListItem> getListItems(int listId, int offset, int rows);

  /**
   * Get a collection of lists items filtering by listId.
   * 
   * @param listId
   *          The list id that will be used to filter
   * @param opaId
   *          The opa id that will be used to filter
   * @return Collection of items of the specified list
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  public UserListItem getListItem(int listId, String opaId);

}
