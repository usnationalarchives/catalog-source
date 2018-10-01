package gov.nara.opa.api.services.impl.user.lists;

import gov.nara.opa.api.dataaccess.user.lists.UserListDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.user.lists.ModifyUserListService;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListErrorCode;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ModifyUserListServiceImpl implements ModifyUserListService {

  private static OpaLogger logger = OpaLogger
      .getLogger(ModifyUserListServiceImpl.class);

  SecureRandom random;

  @Autowired
  private UserListDao userListDao;

  /**
   * Update a Search Result List in the system
   * 
   * @param list
   *          The list that we need to update.
   * @return True if the list was correctly registered, false otherwise.
   */
  @Override
  public ServiceResponseObject updateList(UserList list) {

    UserListErrorCode errorCode = UserListErrorCode.NONE;

    try {
      // Update the list on the database.
      if (!userListDao.update(list)) {
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
   * Duplicate list value check
   * 
   * @param listName
   *          List Name
   * @param accountId
   *          User Account ID
   * @return isDuplicate (true/false)
   */
  @Override
  public boolean isDuplicateList(String listName, int accountId) {

    try {
      // Check for duplicate list
      List<UserList> dupList;
      dupList = userListDao.select(listName, accountId);
      if (dupList.size() > 0) {
        return true;
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return false;
  }

  @Override
  public boolean validateListOwner(String listName, int accountId) {

    try {
      // Check if list exists
      List<UserList> validList;
      validList = userListDao.select(listName, accountId);
      if (validList.size() > 0) {
        return true;
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return false;
  }
}
