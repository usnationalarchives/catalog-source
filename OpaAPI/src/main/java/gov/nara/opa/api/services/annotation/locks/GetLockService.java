package gov.nara.opa.api.services.annotation.locks;

import gov.nara.opa.api.services.ServiceResponseObject;

public interface GetLockService {

  /**
   * Gets an active lock for the provided parameters
   * 
   * @param naId
   * @param objectId
   * @param languageISO
   * @return
   */
  public ServiceResponseObject getLock(String naId, String objectId,
      String languageISO);
}
