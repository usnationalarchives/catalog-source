package gov.nara.opa.api.services.user.lists;

import gov.nara.opa.api.user.lists.UserListItem;

import java.util.List;

/**
 * Interface for the Search Result List service that handles requests from the
 * controller
 */
public interface AddToUserListService {

  /**
   * Duplicate list item batch check
   * 
   * @param opaIds
   * @return List of non-duplicate list opaIds
   */
  public List<UserListItem> getNonDuplicateListOpaIds(int listId,
      String[] opaIds);

  public List<String> getOpaIdsToAddToList(int listId, String[] opaIds);

  /**
   * Method to batch add opaIds to a list
   * 
   * @param opaIdsToAddToList
   * @return total number of opaIds added tot he list
   */
  public int batchAddOpaIdsToList(int listId, List<String> opaIdsToAddToList);

}
