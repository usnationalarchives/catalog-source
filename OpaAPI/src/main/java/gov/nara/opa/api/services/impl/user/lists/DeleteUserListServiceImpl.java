package gov.nara.opa.api.services.impl.user.lists;

import gov.nara.opa.api.dataaccess.user.lists.UserListDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.user.lists.DeleteUserListService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListErrorCode;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DeleteUserListServiceImpl implements DeleteUserListService {
  
  private static OpaLogger logger = OpaLogger.getLogger(DeleteUserListServiceImpl.class);

  SecureRandom random;

  @Autowired
  private UserListDao userListDao;

  @Autowired
  private ViewUserListService viewUserListService;


  /**
   * Remove a opaId item from a specified list
   * 
   * @param opaId
   *          The opaId that we need to delete
   * @param listId
   *          The list that we need to modify
   * @return True if the list was correctly updated, false otherwise.
   */
  public ServiceResponseObject removeFromList(UserList list, String opaId) {

    UserListErrorCode errorCode = UserListErrorCode.NONE;

    try {
      // Update the list on the database.
      if (!userListDao.removeFromList(list, opaId)) {
        errorCode = UserListErrorCode.INTERNAL_ERROR;
        errorCode
            .setErrorMessage("Unable to update list. Contact system administrator");
      }

    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    // Build a return the response object
    ServiceResponseObject responseObject = new ServiceResponseObject(errorCode,
        list);
    return responseObject;

  }

  /**
   * Remove all opaId items from a specified list
   * 
   * @param listId
   *          The list that we need to modify
   * @return True if the list was correctly updated, false otherwise.
   */
  public ServiceResponseObject removeAllFromList(UserList list) {

    UserListErrorCode errorCode = UserListErrorCode.NONE;

    try {
      // Update the list on the database.
      if (!userListDao.removeAllFromList(list)) {
        errorCode = UserListErrorCode.INTERNAL_ERROR;
        errorCode
            .setErrorMessage("Unable to update list. Contact system administrator");
      }

    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    // Build a return the response object
    ServiceResponseObject responseObject = new ServiceResponseObject(errorCode,
        list);
    return responseObject;

  }

  /**
   * Delete a specified list
   * 
   * @param list
   *          The list that we need to delete.
   * @return True if the list was correctly deleted, false otherwise.
   */
  @Override
  public ServiceResponseObject deleteList(UserList list) {

    UserListErrorCode errorCode = UserListErrorCode.NONE;

    try {
      // Delete the list on the database.
      if (!userListDao.delete(list)) {
        errorCode = UserListErrorCode.INTERNAL_ERROR;
        errorCode
            .setErrorMessage("Unable to delete list. Contact system administrator");
      }

    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    // Build and return the response object
    ServiceResponseObject responseObject = new ServiceResponseObject(errorCode,
        list);
    return responseObject;
  }

  @Override
  public ServiceResponseObject deleteAllUserLists(int accountId) {
    UserListErrorCode errorCode = UserListErrorCode.NONE;
    LinkedHashMap<String, Object> responseHashMap = new LinkedHashMap<String, Object>();

    // Attempt to obtain the list from the database
    List<UserList> userLists = viewUserListService.viewMyLists(accountId);

    // Validate target list exists.
    if (userLists == null || userLists.size() == 0) {
      errorCode = UserListErrorCode.LISTS_NOT_FOUND;
    } else {
      responseHashMap.put("Total", userLists.size());
      responseHashMap.put("DeletedLists", userLists);

      for (UserList userList : userLists) {
        // Attempt to delete list
        deleteList(userList);
      }
    }

    // Build and return the response object
    ServiceResponseObject responseObject = new ServiceResponseObject(errorCode,
        responseHashMap);
    return responseObject;
  }

}
