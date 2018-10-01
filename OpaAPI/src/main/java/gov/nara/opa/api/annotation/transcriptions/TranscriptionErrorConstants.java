package gov.nara.opa.api.annotation.transcriptions;

import gov.nara.opa.api.system.ErrorConstants;

public class TranscriptionErrorConstants extends ErrorConstants {
  public static final String transcriptionLocked = "The transcription is locked by another user";
  public static final String duplicateAnnotation = "The new transcription is an exact copy of the old one";
  public static final String lockLimitReached = "The total number of simultaneously locked files allowed for the user has been reached";
  public static final String lockNotFound = "Lock record not found";
  public static final String transcriptionNotFound = "Transcription not found";
  public static final String invalidLock = "Invalid lock";
  public static final String invalidAccountId = "Invalid Account Id";
  public static final String invalidReasonId = "Reason Id is invalid";
  public static final String invalidVersionNumber = "Version Number is invalid";
  public static final String sameTranscription = "Attempting to restore an enabled transcription";
  public static final String inactiveTranscription = "Attempting to remove an inactive transcription version";

}
