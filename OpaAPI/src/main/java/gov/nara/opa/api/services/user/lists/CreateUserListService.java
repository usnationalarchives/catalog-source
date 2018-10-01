package gov.nara.opa.api.services.user.lists;

import gov.nara.opa.api.services.ServiceResponseObject;

/**
 * Interface for the Search Result List service that handles requests from the
 * controller
 */
public interface CreateUserListService {

  /**
   * Create a new List in the system
   * 
   * @param listName
   *          The list name that we are going to create.
   * @param accountId
   *          The account Id of the list
   * @return ServiceResponseObject
   */
  public ServiceResponseObject createList(String listName, int accountId);

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
}
