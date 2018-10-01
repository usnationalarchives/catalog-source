package gov.nara.opa.api.services.impl.moderator;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.locks.AnnotationLockErrorCode;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorConstants;
import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.locks.CreateLockService;
import gov.nara.opa.api.services.annotation.locks.DeleteLockService;
import gov.nara.opa.api.services.annotation.locks.ValidateLockService;
import gov.nara.opa.api.services.impl.annotation.transcriptions.TranscriptionServiceUtils;
import gov.nara.opa.api.services.moderator.RemoveTranscriptionService;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class RemoveTranscriptionServiceImpl implements
    RemoveTranscriptionService {

  private static OpaLogger logger = OpaLogger.getLogger(RemoveTranscriptionServiceImpl.class);

  @Autowired
  private ValidateLockService lockValidator;

  @Autowired
  private CreateLockService lockCreator;

  @Autowired
  private DeleteLockService lockDeleter;

  @Autowired
  private TranscriptionDao transcriptionDao;

  @Autowired
  private AnnotationLogDao annotationLogDao;

  @Override
  public ServiceResponseObject removeTranscription(String naId,
      String objectId, int versionNumber, int reasonId, String notes,
      String sessionId, int accountId) {
    TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    ServiceResponseObject result = null;

    // Retrieve transcription
    try {
      Transcription transcription = transcriptionDao.selectByVersion(naId,
          objectId, versionNumber);
      if (transcription != null) {

        //Check if active
        if(transcription.isStatus()) {
          result = removeTranscription(transcription, reasonId, notes, sessionId,
              accountId);
        } else {
          errorCode = TranscriptionErrorCode.INVALID_PARAMETER;
          errorCode
              .setErrorMessage(TranscriptionErrorConstants.inactiveTranscription);          
        }

      } else {
        errorCode = TranscriptionErrorCode.NOT_FOUND;
        errorCode
            .setErrorMessage(TranscriptionErrorConstants.transcriptionNotFound);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      errorCode = TranscriptionErrorCode.INTERNAL_ERROR;
    }

    if (result == null) {
      result = new ServiceResponseObject(errorCode, resultHashMap);
    }

    return result;
  }

  @Override
  public ServiceResponseObject removeTranscription(Transcription transcription,
      int reasonId, String notes, String sessionId, int accountId) {
    TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();

    ServiceResponseObject responseObject;

    // Acquire lock
    responseObject = lockCreator.create(TranscriptionServiceUtils
        .getLockInstance(accountId, transcription.getNaId(),
            transcription.getObjectId()));
    errorCode = TranscriptionErrorCode
        .valueOf(((AnnotationLockErrorCode) responseObject.getErrorCode())
            .toString());

    if (errorCode == TranscriptionErrorCode.NONE) {
      // Lock was acquired
      AnnotationLock lock = (AnnotationLock) responseObject.getContentMap()
          .get("AnnotationLock");

      try {
        // Remove transcription

        // Get new version
        int version = TranscriptionServiceUtils.getNewVersion(annotationLogDao,
            transcription.getNaId(), transcription.getObjectId());

        // - Disable previous log entry
        // -- Get current one first
        List<AnnotationLogValueObject> activeLogs = annotationLogDao.select("TR",
            transcription.getNaId(), transcription.getObjectId(), true);
        AnnotationLogValueObject currentLogEntry = null;
        if (activeLogs != null && activeLogs.size() > 0) {
          currentLogEntry = activeLogs.get(0);

          if (annotationLogDao.disableByLogId(currentLogEntry.getLogId()) == 0) {
            throw new Exception("Previous log entry not found");
          }
        } else {
          throw new Exception("Previous log entry not found");
        }

        // - Create new log entry
        AnnotationLogValueObject newLogEntry = TranscriptionServiceUtils
            .getNewAnnotationLogEntry(transcription, accountId,
                currentLogEntry, version, currentLogEntry.getAccountId(),
                "REMOVE", sessionId, false, reasonId, notes);
        if (!annotationLogDao.insert(newLogEntry)) {
          throw new Exception("Unable to insert new entry");
        }

        // - Disable transcription
        transcription.setStatus(false);
        transcriptionDao.update(transcription);

        // Release lock
        responseObject = lockDeleter.delete(lock.getNaId(), lock.getObjectId(),
            null, accountId);
        errorCode = TranscriptionErrorCode
            .valueOf(((AnnotationLockErrorCode) responseObject.getErrorCode())
                .toString());

      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        errorCode = TranscriptionErrorCode.INTERNAL_ERROR;
      }
    }

    ServiceResponseObject result = new ServiceResponseObject(errorCode,
        resultHashMap);
    return result;
  }

}
