package gov.nara.opa.api.services.ingestion;

/**
 * Interface for the Search Result List service that handles requests from the
 * controller
 */
public interface UpdatePageNumService {

  /**
   * Update a Search Result List in the system
   * 
   * @param list
   *          The list that we need to update.
   * @return True if the list was correctly registered, false otherwise.
   */
  public String updatePageNum(String naId, String objectId, int pageNum);

}
