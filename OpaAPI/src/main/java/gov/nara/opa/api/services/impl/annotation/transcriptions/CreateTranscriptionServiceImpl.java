package gov.nara.opa.api.services.impl.annotation.transcriptions;

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
import gov.nara.opa.api.services.annotation.transcriptions.CreateTranscriptionService;
import gov.nara.opa.api.services.annotation.transcriptions.ViewTranscriptionService;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CreateTranscriptionServiceImpl implements
    CreateTranscriptionService {
  
  private static OpaLogger logger = OpaLogger.getLogger(CreateTranscriptionServiceImpl.class);

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

  @Autowired
  private ViewTranscriptionService viewTranscriptionService;

  
  /*
   * (non-Javadoc)
   * 
   * @see gov.nara.opa.api.services.annotation.transcriptions.
   * CreateTranscriptionService#lock(int, java.lang.String, java.lang.String)
   */
  @Override
  public ServiceResponseObject lock(int accountId, String naId, String objectId) {
    // Create new lock
    AnnotationLock lock = new AnnotationLock();
    lock.setAccountId(accountId);
    lock.setNaId(naId);
    lock.setObjectId(objectId);

    ServiceResponseObject responseObject = lockCreator.create(lock);

    return responseObject;
  }

  @Override
  public ServiceResponseObject saveAndRelock(Transcription transcription,
      String sessionId, int accountId) {
    return save(transcription, sessionId, false, accountId);
  }

  @Override
  public ServiceResponseObject saveAndUnlock(Transcription transcription,
      String sessionId, int accountId) {
    return save(transcription, sessionId, true, accountId);
  }

  /**
   * Abstracts the save call and implements both relock and unlock
   * 
   * @param transcription
   * @param unlock
   * @return
   */
  private ServiceResponseObject save(Transcription transcription,
      String sessionId, boolean unlock, int accountId) {
    TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();

    try {

      
      // Get current active annotation log
      List<AnnotationLogValueObject> activeLogs = annotationLogDao.select("TR",
          transcription.getNaId(), transcription.getObjectId(), true);
      AnnotationLogValueObject currentAnnotationLog = null;
      if (activeLogs != null && activeLogs.size() > 0) {
        currentAnnotationLog = activeLogs.get(0);
      }

      int firstAnnotationId = 0;
      int firstAccountId = 0;

      // Validate md5 to make sure transcription content has actually
      // changed from previous version
      if (currentAnnotationLog != null) {

        if (currentAnnotationLog.getAnnotationMD5().equals(
            transcription.getAnnotationMD5())) {
          errorCode = TranscriptionErrorCode.DUPLICATE_ANNOTATION;
          errorCode
              .setErrorMessage(TranscriptionErrorConstants.duplicateAnnotation);
        } else {
          firstAnnotationId = currentAnnotationLog.getFirstAnnotationId();
          firstAccountId = currentAnnotationLog.getFirstAccountId();
          
          transcription.setFirstAnnotationId(firstAnnotationId);
        }
      } else {
        // If there was no active log, look for inactive ones to get first
        // annotation and
        // first account ids.
        List<AnnotationLogValueObject> inactiveLogs = annotationLogDao.select("TR",
            transcription.getNaId(), transcription.getObjectId(), false);
        if (inactiveLogs != null && inactiveLogs.size() > 0) {
          firstAnnotationId = inactiveLogs.get(0).getFirstAnnotationId();
          firstAccountId = inactiveLogs.get(0).getFirstAccountId();

          transcription.setFirstAnnotationId(firstAnnotationId);
        }
      }

      if (errorCode == TranscriptionErrorCode.NONE) {
        // Determine affected user
        int affectedUser = getAffectedUserId(currentAnnotationLog,
            transcription.getAccountId());

        // Determine action value
        String action = getActionValue(currentAnnotationLog);

        // Get new version number
        int version = TranscriptionServiceUtils.getNewVersion(annotationLogDao,
            transcription.getNaId(), transcription.getObjectId());

        // Deactivate current active transcription
        // Deactivate current active annotation log entry
        boolean ok = true;
        if (currentAnnotationLog != null) {
          // Get current transcription
          Transcription currentTranscription = transcriptionDao
              .select(currentAnnotationLog.getAnnotationId());
          if (currentTranscription == null) {
            ok = false;
          } else {
            // Set transcription and annotation statuses to false
            // and 0 accordingly
            currentTranscription.setStatus(false);
            ok = transcriptionDao.update(currentTranscription);
            currentAnnotationLog.setStatus(false);
            ok = ok && annotationLogDao.update(currentAnnotationLog);
          }
        }

        if (ok) {
          // Insert new transcription
          transcription.setSavedVersNum(version);
          ok = transcriptionDao.insert(transcription);

          // Get annotation Id
          Transcription newTranscription = transcriptionDao.select(
              transcription.getNaId(), transcription.getObjectId(), 1);
          if (newTranscription != null && ok) {

            // Create annotation log entry for the new transcription
        	  AnnotationLogValueObject newLogEntry = null;
            if (currentAnnotationLog != null) {
              newLogEntry = TranscriptionServiceUtils.getNewAnnotationLogEntry(
                  newTranscription, accountId, currentAnnotationLog, version,
                  affectedUser, action, sessionId);
            } else {
              if (firstAnnotationId == 0 || firstAccountId == 0) {
                firstAnnotationId = newTranscription.getAnnotationId();
                firstAccountId = newTranscription.getAccountId();
              }

              newLogEntry = TranscriptionServiceUtils.getNewAnnotationLogEntry(
                  newTranscription, accountId, firstAnnotationId,
                  firstAccountId, version, affectedUser, action, sessionId);
            }

            ok = annotationLogDao.insert(newLogEntry);

            if (ok) {
              // Update or release lock
              if (unlock) {
                // Unlock
                ServiceResponseObject responseObject = lockDeleter.delete(
                    newTranscription.getNaId(), newTranscription.getObjectId(),
                    null, accountId);
                AnnotationLockErrorCode lockErrorCode = (AnnotationLockErrorCode) responseObject
                    .getErrorCode();
                if (lockErrorCode != AnnotationLockErrorCode.NONE) {
                  ok = false;
                }

              } else {
                // Update lock
                AnnotationLock lock = new AnnotationLock();
                lock.setAccountId(newTranscription.getAccountId());
                lock.setNaId(newTranscription.getNaId());
                lock.setObjectId(newTranscription.getObjectId());

                ServiceResponseObject responseObject = lockCreator.create(lock);
                AnnotationLockErrorCode lockErrorCode = (AnnotationLockErrorCode) responseObject
                    .getErrorCode();
                if (lockErrorCode != AnnotationLockErrorCode.NONE) {
                  ok = false;
                }
              }

              // If all went well, retrieve the full transcription
              // information
              if (ok) {
                ServiceResponseObject responseObject = viewTranscriptionService
                    .getFullTranscription(newTranscription.getNaId(),
                        newTranscription.getObjectId());
                errorCode = (TranscriptionErrorCode) responseObject
                    .getErrorCode();
                if (errorCode == TranscriptionErrorCode.NONE) {
                  HashMap<String, Object> results = (HashMap<String, Object>) responseObject
                      .getContentMap();
                  resultHashMap.putAll(results);

                }
              }
            }
          }
        }

        if (!ok) {
          errorCode = TranscriptionErrorCode.INTERNAL_ERROR;
          errorCode
              .setErrorMessage(TranscriptionErrorConstants.internalDatabaseError);
        }
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      errorCode = TranscriptionErrorCode.INTERNAL_ERROR;
      errorCode.setErrorMessage(e.getMessage());
    }

    ServiceResponseObject result = new ServiceResponseObject(errorCode,
        resultHashMap);
    return result;
  }

  /**
   * If there's no active annotation log entry or the active entry was a remove
   * action then the affected user is the new transcriber otherwise it's the
   * current log entry's user
   * 
   * @param currentAnnotationLog
   *          The current active annotation log entry
   * @param transcriberId
   *          The ID of the user that created the new transcription
   * @return
   */
  private int getAffectedUserId(AnnotationLogValueObject currentAnnotationLog,
      int transcriberId) {
    int result = 0;
    if (currentAnnotationLog == null
        || currentAnnotationLog.getAction().equals("REMOVE")) {
      result = transcriberId;
    } else {
      result = currentAnnotationLog.getAccountId();
    }

    return result;
  }

  /**
   * If there's no active annotation log entry or the active entry was a remove
   * action then the new action is ADD otherwise it's UPDATE
   * 
   * @param currentAnnotationLog
   * @return
   */
  private String getActionValue(AnnotationLogValueObject currentAnnotationLog) {
    String result = null;

    if (currentAnnotationLog == null
        || currentAnnotationLog.getAction().equals("REMOVE")) {
      result = "ADD";
    } else {
      result = "UPDATE";
    }

    return result;
  }

}
