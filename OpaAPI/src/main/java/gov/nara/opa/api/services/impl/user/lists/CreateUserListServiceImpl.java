package gov.nara.opa.api.services.impl.user.lists;

import gov.nara.opa.api.dataaccess.user.lists.UserListDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.services.user.lists.CreateUserListService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListErrorCode;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CreateUserListServiceImpl implements CreateUserListService {

  private static OpaLogger logger = OpaLogger
      .getLogger(CreateUserListServiceImpl.class);

  SecureRandom random;

  @Autowired
  private UserListDao userListDao;

  @Autowired
  private ViewUserListService viewUserListService;
  
  @Autowired
  private ConfigurationService configurationService;

  /**
   * Create a new List in the system
   * 
   * @param list
   *          The list name that we are going to create.
   * @param accountId
   *          The account Id of the list
   * @return True if the list was correctly registered, false otherwise.
   */
  @Override
  public ServiceResponseObject createList(String listName, int accountId) {
    UserListErrorCode errorCode = UserListErrorCode.NONE;
    UserList newList = null;

    try {
      //Validate the user list number limit hasn't been reached
      int listCount = userListDao.getUserListCount(accountId);
      int maxLists = configurationService.getConfig().getMaxListsPerUser();  
      
      if(listCount < maxLists) {
      
      newList = new UserList(listName, accountId);

        // Initialize default fields
        initializeListValues(newList);
  
        // Call the data access layer to execute the insert
        boolean status = userListDao.create(newList);
  
        if (status) {
          newList = viewUserListService.getList(listName, accountId);
        } else {
          errorCode = UserListErrorCode.INTERNAL_ERROR;
          errorCode.setErrorMessage("Unable to create the List.");
        }
      
      } else {
        errorCode = UserListErrorCode.MAX_LIST_NUMBER_REACHED;
        errorCode.setErrorMessage("You have reached the maximum number of lists allowed per user.");
      }

    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    ServiceResponseObject responseObject = new ServiceResponseObject(errorCode,
        newList);
    return responseObject;
  }

  /**
   * Initialize default values
   * 
   * @param userList
   *          The object with the data that we are going to save.
   */
  private void initializeListValues(UserList userList) {

    userList.setListName(userList.getListName());
    userList.setAccountId(userList.getAccountId());
    userList.setCreatedTs(new Timestamp(DateTime.now().toDate().getTime()));

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

}
