package gov.nara.opa.api.services.moderator;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.services.ServiceResponseObject;

public interface RemoveTranscriptionService {

  /**
   * Disables a transcription and logs the transaction
   * 
   * @param naId
   * @param objectId
   * @param reasonId
   *          The removal reason Id
   * @param notes
   *          The removal reason note
   * @param sessionId
   * @return The removed transcription with the key Transcription in the
   *         response object's hash map or a valid error code
   */
  ServiceResponseObject removeTranscription(String naId, String objectId,
      int versionNumber, int reasonId, String notes, String sessionId,
      int accountId);

  /**
   * Disables a transcription and logs the transaction
   * 
   * @param transcription
   * @param reasonId
   *          The removal reason Id
   * @param notes
   *          The removal reason note
   * @param sessionId
   * @return The removed transcription with the key Transcription in the
   *         response object's hash map or a valid error code
   */
  ServiceResponseObject removeTranscription(Transcription transcription,
      int reasonId, String notes, String sessionId, int accountId);

}
