package gov.nara.opa.api.services.user.lists;

import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.user.lists.UserList;

/**
 * Interface for the Search Result List service that handles requests from the
 * controller
 */
public interface DeleteUserListService {

  /**
   * Delete a specified list
   * 
   * @param list
   *          The list that we need to delete.
   * @return True if the list was correctly deleted, false otherwise.
   */
  public ServiceResponseObject deleteList(UserList list);

  /**
   * Remove all opaId items from a specified list
   * 
   * @param listId
   *          The list that we need to modify
   * @return True if the list was correctly updated, false otherwise.
   */
  public ServiceResponseObject removeAllFromList(UserList list);

  /**
   * Remove a opaId item from a specified list
   * 
   * @param opaId
   *          The opaId that we need to delete
   * @param listId
   *          The list that we need to modify *
   * @return True if the list was correctly updated, false otherwise.
   */
  public ServiceResponseObject removeFromList(UserList list, String opaId);

  /**
   * Deletes all user lists and their items for a given account id
   * 
   * @param accountId
   * @return
   */
  public ServiceResponseObject deleteAllUserLists(int accountId);

}
