package gov.nara.opa.api.services.moderator;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.services.ServiceResponseObject;

public interface RestoreTranscriptionService {

  /**
   * Restores a disabled transcription and logs an entry in the transaction
   * table
   * 
   * @param naId
   * @param objectId
   * @param reasonId
   *          The reason Id for the restoration
   * @param notes
   *          The notes for the restoration
   * @param sessionId
   * @return The restored transcription with the key Transcription in the
   *         response object's hash map or a valid error code
   */
  ServiceResponseObject restoreTranscription(String naId, String objectId,
      int version, int reasonId, String notes, String sessionId, int accountId);

  /**
   * Restores a disabled transcription and logs an entry in the transaction
   * table
   * 
   * @param transcription
   * @param reasonId
   *          The reason Id for the restoration
   * @param notes
   *          The notes for the restoration
   * @param sessionId
   * @return The restored transcription with the key Transcription in the
   *         response object's hash map or a valid error code
   */
  ServiceResponseObject restoreTranscription(Transcription transcription,
      int reasonId, String notes, String sessionId, int accountId);

}
