package gov.nara.opa.api.services.moderator;

import gov.nara.opa.api.services.ServiceResponseObject;

public interface ViewModeratorStreamService {

  /**
   * Returns the moderator stream limited by the number of rows starting from
   * offset. Also filtered by annotation type and naId
   * 
   * @param offset
   * @param rows
   * @param filterType
   * @param naId
   * @return A content hash map with the following keys: Stream - The stream
   *         collection
   */
  public ServiceResponseObject viewModeratorStream(int offset, int rows,
      String filterType, String naId);

  /**
   * Returns the total number of contributions for the last 6 months
   * 
   * @param offset
   * @param rows
   * @param filterType
   * @param naId
   * @return A linked hash map with the following keys and values as ints: -
   *         TagTotal - TranscriptionTotal
   */
  public ServiceResponseObject viewContributionTotals(String naId);

}
