/**
 * 
 */
package gov.nara.opa.api.services.impl.user.lists;

import gov.nara.opa.api.dataaccess.user.lists.UserListDao;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.api.valueobject.user.lists.UserListCollectionValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ViewUserListServiceImpl implements ViewUserListService {

  private static OpaLogger logger = OpaLogger
      .getLogger(ViewUserListServiceImpl.class);

  SecureRandom random;

  @Autowired
  private UserListDao userListDao;

  /**
   * 
   * Select and return the list from the database using the parameter listname.
   * 
   * @param listName
   *          The name of the list that we want to select.
   * @return The selected list or null if not found.
   */
  @Override
  public UserList getList(String listName) {
    UserList userList = null;

    try {
      // Call the JdcTemplate method to select the list filtering by name.
      List<UserList> resultList = userListDao.select(listName);
      userList = (resultList != null && resultList.size() > 0 ? resultList
          .get(0) : null);

    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return userList;
  }

  /**
   * 
   * Select and return the list from the database using the parameter listname.
   * 
   * @param listName
   *          The name of the list that we want to select.
   * @param accountId
   *          The account id that we want to filter by.
   * @return The selected list or null if not found.
   */
  @Override
  public UserList getList(String listName, int accountId) {
    UserList userList = null;

    try {
      // Call the JdcTemplate method to select the list filtering by name.
      List<UserList> resultList = userListDao.select(listName, accountId);
      userList = (resultList != null && resultList.size() > 0 ? resultList
          .get(0) : null);

    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return userList;
  }

  /**
   * Retrieve all lists for this accountId
   * 
   * @param accountId
   *          The accountId we are filtering by.
   * @return Collection of lists for the specified accountId
   */
  @Override
  public List<UserList> viewMyLists(int accountId) {
    List<UserList> resultList = null;

    try {
      // Call the JdcTemplate method to select the list filtering by name.
      resultList = userListDao.selectByAccountId(accountId);
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return resultList;
  }

  /**
   * Retrieve all lists for this accountId
   * 
   * @param accountId
   *          The accountId we are filtering by
   * @param offset
   *          The offset specifies the offset of the first row to return
   * @param rows
   *          The count specifies maximum number of rows to return
   * @return Collection of lists for the specified accountId
   */
  @Override
  public UserListCollectionValueObject viewMyLists(int accountId, int offset, int rows) {
    List<UserList> resultList = null;
    Integer totalLists = 0;

    try {
      // Call the JdcTemplate method to select the list filtering by name.
      resultList = userListDao.selectByAccountId(accountId, offset, rows);
      
      totalLists = userListDao.getUserListCount(accountId);
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    UserListCollectionValueObject resultObject = new UserListCollectionValueObject();
    resultObject.setListCollection(resultList);
    resultObject.setTotalLists(totalLists);
    
    return resultObject;
  }

  /**
   * Get a collection of lists items filtering by listId.
   * 
   * @param listId
   *          The list id that will be used to filter
   * @return Collection of items of the specified list
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  @Override
  public List<UserListItem> getListItems(int listId) {

    try {
      // Store selected items on the collection
      List<UserListItem> result = userListDao.selectListItems(listId);
      if (result.size() > 0) {
        return result;
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return null;
  }

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
  @Override
  public List<UserListItem> getListItems(int listId, int offset, int rows) {

    try {

      // Store selected items on the collection
      List<UserListItem> result = userListDao.selectListItems(listId, offset,
          rows);
      if (result.size() > 0) {
        return result;
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return null;
  }

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
  public UserListItem getListItem(int listId, String opaId) {

    try {
      // Store selected items on the collection
      List<UserListItem> result = userListDao.selectListItem(listId, opaId);
      if (result.size() > 0) {
        return result.get(0);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return null;
  }

}
