package gov.nara.opa.api.services.annotation.locks;

public interface ValidateLockService {

  /**
   * Validates that a lock has been acquired by the requesting user
   * 
   * @param accountId
   *          The requesting user account Id
   * @param naId
   *          The item Id
   * @param objectId
   *          The digital object id
   * @param languageISO
   *          The language
   * @return True if the lock is valid, false otherwise
   */
  boolean validateLock(int accountId, String naId, String objectId,
      String languageISO);

  /**
   * Determines if a resource can be locked by the requesting user
   * 
   * @param naId
   *          The item naId
   * @param objectId
   *          The digital object id
   * @param languageISO
   *          The language
   * @return True if the resource can be locked, false if a lock already exists
   */
  boolean canLock(String naId, String objectId, String languageISO);

}
