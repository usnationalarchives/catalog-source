package gov.nara.opa.api.services.user.lists;

import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.user.lists.UserList;

/**
 * Interface for the Search Result List service that handles requests from the
 * controller
 */
public interface ModifyUserListService {

  /**
   * Update a Search Result List in the system
   * 
   * @param list
   *          The list that we need to update.
   * @return True if the list was correctly registered, false otherwise.
   */
  public ServiceResponseObject updateList(UserList list);

  /**
   * Duplicate list value check
   * 
   * @param listName
   *          List Name
   * @param accountId
   *          User Account ID
   * @return isDuplicate (true/false)
   */
  public boolean isDuplicateList(String listName, int accountId);

  /**
   * @param listName
   *          List Name
   * @param accountId
   *          User Account ID
   * @return isOwner (true/false)
   */
  public boolean validateListOwner(String listName, int accountId);
}
