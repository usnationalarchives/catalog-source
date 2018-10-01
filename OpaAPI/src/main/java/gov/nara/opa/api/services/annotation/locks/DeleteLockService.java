package gov.nara.opa.api.services.annotation.locks;

import gov.nara.opa.api.services.ServiceResponseObject;

public interface DeleteLockService {

  /**
   * Deletes all existing records for the provided values
   * 
   * @param naId
   *          The naId of the lock records
   * @param objectId
   *          The digital objectId
   * @param languageISO
   *          The language ISO value
   * @param accountId
   *          The id of the unlocking user
   * @return A response object with a valid error code if failed and the
   *         pertinent object instances in the content
   */
  public ServiceResponseObject delete(String naId, String objectId,
      String languageISO, int accountId);
  
}
