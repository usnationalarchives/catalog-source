package gov.nara.opa.api.dataaccess.user.lists;

import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListItem;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;

public interface UserListDao {

  /**
   * Insert a new list
   * 
   * @param userList
   *          The list that we attempt to insert
   * @return true if the list was inserted, otherwise false
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  boolean create(UserList userList) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Get a collection of lists filtering by listId.
   * 
   * @param listId
   *          The account Id that will be used to filter
   * @return Collection of lists filtered by listId
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<UserList> select(int listId) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Get a collection of lists filtering by listName.
   * 
   * @param listName
   *          The listName that will be used to filter
   * @return Collection of lists with the specified name
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<UserList> select(String listName) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Get a collection of lists filtering by listName and accountId.
   * 
   * @param listName
   *          The listName that will be used to filter
   * @param accountId
   *          The account id that will be used to filter
   * @return Collection of lists with the specified name
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<UserList> select(String listName, int accountId)
      throws DataAccessException, UnsupportedEncodingException;

  /**
   * Get a collection of lists filtering by accountId.
   * 
   * @param accountId
   *          The account id that will be used to filter
   * @return Collection of lists with the specified name
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<UserList> selectByAccountId(int accountId) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Get a collection of lists filtering by accountId.
   * 
   * @param accountId
   *          The account id that will be used to filter
   * @param offset
   *          The offset specifies the offset of the first row to return
   * @param rows
   *          The count specifies maximum number of rows to return
   * @return Collection of lists with the specified name
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<UserList> selectByAccountId(int accountId, int offset, int rows)
      throws DataAccessException, UnsupportedEncodingException;

  /**
   * Update an existing list
   * 
   * @param userList
   *          The list with the data that we want to update.
   * @return true if the list was updated, otherwise false
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  boolean update(UserList userList) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Remove a opaId item from a specified list
   * 
   * @param opaId
   *          The opaId that we need to delete
   * @param listId
   *          The list that we need to modify *
   * @return True if the list was correctly updated, false otherwise.
   */
  boolean removeFromList(UserList userList, String opaId)
      throws DataAccessException, UnsupportedEncodingException;

  /**
   * Remove all opaId items from a specified list
   * 
   * @param listId
   *          The list that we need to modify
   * @return True if the list was correctly updated, false otherwise.
   */
  boolean removeAllFromList(UserList userList) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Delete existing list
   * 
   * @param userList
   *          The list with the data that we want to delete.
   * @return true if the list was deleted, otherwise false
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  boolean delete(UserList userList) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Return the item contained on the specified list with the specified opaId
   * 
   * @param opaId
   *          opaId of the item we need to get
   * @param listId
   *          listId of the specified list
   * @return Collection of items that fullfill the filters setted
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<UserListItem> selectListItem(int listId, String opaId)
      throws DataAccessException, UnsupportedEncodingException;

  /**
   * Return a collection of items contained on a specified list
   * 
   * @param listId
   *          Id of the list to be consulted
   * @param offset
   *          The offset specifies the offset of the first row to return
   * @param rows
   *          The count specifies maximum number of rows to return
   * @return Collection of the item contained
   */
  public List<UserListItem> selectListItems(int listId, int offset, int rows);

  /**
   * Return a collection of items contained on a specified list
   * 
   * @param listId
   *          Id of the list to be consulted
   * @return Collection of the item contained
   */
  public List<UserListItem> selectListItems(int listId);

  /**
   * Return the number of lists that a user has
   * 
   * @param accountId
   *          The user Id
   * @return The number of user lists
   */
  public int getUserListCount(int accountId) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Return the number of list items that a list has
   * 
   * @param listId
   *          The user list Id
   * @return The number of list items
   */
  public int getUserListItemCount(int listId) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Checks a list for duplicate opaIds
   * 
   * @param opaIds
   *          List of opaIds to check
   * @return List of non-duplicate Ids
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  public List<UserListItem> checkListForDuplicateOpaIds(int listId,
      String[] opaIds) throws DataAccessException, UnsupportedEncodingException;

  public List<String> checkListForOpaIds(int listId, String[] opaIds)
      throws DataAccessException, UnsupportedEncodingException;

  /**
   * Batch add opaIds to a given list
   * 
   * @param opaIdsToAddToList
   * @return total items added to a list
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  public int batchAddToUserList(int listId, List<String> opaIdsToAddToList)
      throws DataAccessException, UnsupportedEncodingException;

}
