package gov.nara.opa.api.services.annotation.transcriptions;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.services.ServiceResponseObject;

public interface CreateTranscriptionService {

  /**
   * Encapsulates a lock creation call
   * 
   * @param accountId
   * @param naId
   * @param objectId
   * @return A valid AnnotationLock instance and related UserAccount within the
   *         content hash map. May return a Transcription and previous
   *         TranscriptionsByUser along with past users as ContributorMap or a
   *         valid error code
   */
  public ServiceResponseObject lock(int accountId, String naId, String objectId);

  /**
   * Saves a transcription and keeps the lock
   * 
   * @param transcription
   * @return A LinkedHashMap with Transcription as key for a trasncription
   *         instance, UserAccount as key for the current user, AnnotationLock
   *         as key for the current lock instance, TranscriptionsByUser with the
   *         previous contributions by user in a LinkedHashMap, ContributorsMap
   *         as key as a LinkedHashMap with the contributor UserAccounts OR a
   *         valid error code
   */
  public ServiceResponseObject saveAndRelock(Transcription transcription,
      String sessionId, int accountId);

  /**
   * Saves a transcription and deletes the lock
   * 
   * @param transcription
   * @return A LinkedHashMap with Transcription as key for a trasncription
   *         instance, UserAccount as key for the current user,
   *         TranscriptionsByUser with the previous contributions by user in a
   *         LinkedHashMap, ContributorsMap as key as a LinkedHashMap with the
   *         contributor UserAccounts OR a valid error code
   */
  public ServiceResponseObject saveAndUnlock(Transcription transcription,
      String sessionId, int accountId);

}
