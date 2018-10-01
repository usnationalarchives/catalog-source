package gov.nara.opa.api.usagelogging.annotation.transcriptions;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.common.AnnotationConstants;

public class TranscriptionsLogger {
  private static OpaLogger log = OpaLogger
      .getLogger(TranscriptionsLogger.class);

  private static final String ADDED_TRANSCRIPTION = "Action=%1$s, AnnotationType=%2$s, "
      + "Naid=%3$s, Object=%4$s";

  public static void logTranscription(Transcription transcription,
      Class<?> controller, String typeString, String action) {

    String naId = transcription.getNaId();
    String objectId = transcription.getObjectId() == null ? "" : transcription
        .getObjectId();
   
    log.usage(controller, (typeString.equals("iapi") ? ApiTypeLoggingEnum.API_TYPE_INTERNAL : ApiTypeLoggingEnum.API_TYPE_PUBLIC), 
        UsageLogCode.TRANSCRIPTION, String.format(ADDED_TRANSCRIPTION,
        action,
        AnnotationConstants.ANNOTATION_TYPE_TRANSCRIPTION_LOGS, naId,
        objectId));

  }

}
