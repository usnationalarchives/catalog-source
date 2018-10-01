package gov.nara.opa.api.services.annotation.transcriptions;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.services.ServiceResponseObject;

public interface ViewTranscriptionService {

  /**
   * Gets a transcription by its annotation Id
   * 
   * @param annotationId
   * @return
   */
  public ServiceResponseObject getTranscription(int annotationId);

  /**
   * Gets the currently active transcription for an naId and object Id
   * 
   * @param naId
   * @param objectId
   * @return
   */
  public ServiceResponseObject getActiveTranscription(String naId,
      String objectId);

  /**
   * Gets a transcription by version number
   * 
   * @param naId
   * @param objectId
   * @param versionNumber
   * @return The transcription with key Transcription in the return hash map or
   *         an error code
   */
  public ServiceResponseObject getTranscriptionByVersion(String naId,
      String objectId, int versionNumber);

  /**
   * Gets a transcription by naIds
   * 
   * @param naId
   * @return The transcription with key Transcription in the return hash map or
   *         an error code
   */
  public ServiceResponseObject selectByNaIds(String[] naIds);

  /**
   * Retrieves a collection of log entries for a transcription identified by an
   * naId and an objectId
   * 
   * @param naId
   * @param objectId
   * @return The version List of AnnotationLog instances with key Versions
   *         inside the response object ContentMap or an error code.
   */
  public ServiceResponseObject getAllTranscriptionVersions(String naId,
      String objectId);

  /**
   * Returns all transcriptions derived from a first annotation Id regardless of
   * status
   * 
   * @param firstAnnotationId
   * @return The response object will contain a LinkedHashMap with the following
   *         keys: TranscriptionsByUser for the transcription list, UserMap for
   *         the list of contributors.
   */
  public ServiceResponseObject getDerivedTranscriptions(int firstAnnotationId);

  /**
   * Returns all transcriptions derived from a first annotation Id with a
   * provided status
   * 
   * @param firstAnnotationId
   * @param status
   * @return The response object will contain a LinkedHashMap with the following
   *         keys: TranscriptionsByUser for the transcription list, UserMap for
   *         the list of contributors.
   */
  public ServiceResponseObject getDerivedTranscriptions(int firstAnnotationId,
      int status);

  /**
   * Gets a transcription plus all its related objects
   * 
   * @param naId
   * @param objectId
   * @return Response object with valid error code if failed or a
   *         HashMap<String, Object> with the following keys: Transcription -
   *         the transcription instance. TranscriptionsByUser - the previous
   *         transcriptions for this naId+objectId. UserMap - a HashMap<integer,
   *         UserAccount> with the authors of the previous transcriptions.
   *         AnnotationLock - the current lock information if any. UserAccount -
   *         the current lock user
   */
  public ServiceResponseObject getFullTranscription(String naId, String objectId);

  /**
   * Gets a transcription plus all its related objects
   * 
   * @param firstAnnotationId
   * @param userName
   * @return Response object with valid error code if failed or a
   *         HashMap<String, Object> with the following keys: Transcription -
   *         the transcription instance. TranscriptionsByUser - the previous
   *         transcriptions for this naId+objectId. UserMap - a HashMap<integer,
   *         UserAccount> with the authors of the previous transcriptions.
   *         AnnotationLock - the current lock information if any. UserAccount -
   *         the current lock user
   */
  public Transcription getLastOtherUserModifiedTranscription(
      int firstAnnotationId, String userName);

  /**
   * Gets a transcription plus all its related objects
   * 
   * @param firstAnnotationId
   * @param userName
   * @return Response object with valid error code if failed or a
   *         HashMap<String, Object> with the following keys: Transcription -
   *         the transcription instance. TranscriptionsByUser - the previous
   *         transcriptions for this naId+objectId. UserMap - a HashMap<integer,
   *         UserAccount> with the authors of the previous transcriptions.
   *         AnnotationLock - the current lock information if any. UserAccount -
   *         the current lock user
   */
  public Transcription selectLastOwnerModifiedTranscription(
      int firstAnnotationId, String userName);

  /**
   * Gets a transcription plus all its related objects
   * 
   * @param naId
   * @param objectId
   * @param versionNumber
   * @return Response object with valid error code if failed or a
   *         HashMap<String, Object> with the following keys: Transcription -
   *         the transcription instance. TranscriptionsByUser - the previous
   *         transcriptions for this naId+objectId. UserMap - a HashMap<integer,
   *         UserAccount> with the authors of the previous transcriptions.
   *         AnnotationLock - the current lock information if any. UserAccount -
   *         the current lock user
   */
  public ServiceResponseObject getFullTranscription(String naId,
      String objectId, int versionNumber);
}
