package gov.nara.opa.api.services.impl.annotation.transcriptions;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.locks.AnnotationLockErrorCode;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorConstants;
import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.api.dataaccess.user.accounts.OldUserAccountDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.locks.CreateLockService;
import gov.nara.opa.api.services.annotation.locks.GetLockService;
import gov.nara.opa.api.services.annotation.locks.ValidateLockService;
import gov.nara.opa.api.services.annotation.transcriptions.ViewTranscriptionService;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ViewTranscriptionServiceImpl implements ViewTranscriptionService {

  private static OpaLogger logger = OpaLogger
      .getLogger(ViewTranscriptionServiceImpl.class);

  @Autowired
  private ValidateLockService lockValidator;

  @Autowired
  private CreateLockService lockCreator;

  @Autowired
  private GetLockService lockGetter;

  @Autowired
  private TranscriptionDao transcriptionDao;

  @Autowired
  private AnnotationLogDao annotationLogDao;

  @Autowired
  private OldUserAccountDao userAccountDao;

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nara.opa.api.services.annotation.transcriptions.ViewTranscriptionService
   * #getTranscription(int)
   */
  @Override
  public ServiceResponseObject getTranscription(int annotationId) {
    TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    Transcription transcription = null;

    try {
      transcription = transcriptionDao.select(annotationId);

      if (transcription != null) {
        resultHashMap.put("Transcription", transcription);
      }

    } catch (BadSqlGrammarException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    } catch (DataAccessException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return new ServiceResponseObject(errorCode, resultHashMap);
  }

  @Override
  public Transcription selectLastOwnerModifiedTranscription(
      int firstAnnotationId, String userName) {
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    Transcription transcription = null;

    try {
      transcription = transcriptionDao.selectLastOwnerModifiedTranscription(
          firstAnnotationId, userName);

      if (transcription != null) {
        resultHashMap.put("Transcription", transcription);
      }

    } catch (BadSqlGrammarException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    } catch (DataAccessException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return transcription;
  }

  @Override
  public Transcription getLastOtherUserModifiedTranscription(
      int firstAnnotationId, String userName) {
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    Transcription transcription = null;

    try {
      transcription = transcriptionDao
          .selectLastOtherUserModifiedTranscription(firstAnnotationId, userName);

      if (transcription != null) {
        resultHashMap.put("Transcription", transcription);
      }

    } catch (BadSqlGrammarException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    } catch (DataAccessException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return transcription;
  }

  @Override
  public ServiceResponseObject getActiveTranscription(String naId,
      String objectId) {
    TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    Transcription transcription = null;

    try {
      transcription = transcriptionDao.select(naId, objectId, 1);
      if (transcription != null) {
        resultHashMap.put("Transcription", transcription);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return new ServiceResponseObject(errorCode, resultHashMap);
  }

  @Override
  public ServiceResponseObject selectByNaIds(String[] naIds) {
    TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    List<Transcription> transcriptions = null;

    try {
      transcriptions = transcriptionDao.selectByNaIds(naIds);
      if (transcriptions != null) {
        resultHashMap.put("Transcriptions", transcriptions);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return new ServiceResponseObject(errorCode, resultHashMap);
  }

  @Override
  public ServiceResponseObject getTranscriptionByVersion(String naId,
      String objectId, int versionNumber) {
    TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    Transcription transcription = null;

    try {
      //Get actual version from annotation log
      AnnotationLogValueObject log = annotationLogDao.selectByVersion("TR", naId, objectId, versionNumber);
      
      if(log != null) {
        int transcriptionId = log.getAnnotationId();
        
        transcription = transcriptionDao.select(transcriptionId);
      }

      if (transcription != null) {
        resultHashMap.put("Transcription", transcription);
      } else {
        errorCode = TranscriptionErrorCode.NOT_FOUND;
        errorCode
            .setErrorMessage(TranscriptionErrorConstants.transcriptionNotFound);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return new ServiceResponseObject(errorCode, resultHashMap);
  }

  @Override
  public ServiceResponseObject getDerivedTranscriptions(int firstAnnotationId) {
    return getDerivedTranscriptions(firstAnnotationId, -1);
  }

  @Override
  public ServiceResponseObject getDerivedTranscriptions(int firstAnnotationId,
      int status) {
    TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    List<Transcription> transcriptions = null;

    try {
      String whereClause;
      Object[] paramArray;
      if (status != -1) {
        whereClause = "WHERE first_annotation_id = ? AND status = ? ORDER BY annotation_ts DESC";
        paramArray = new Object[] { firstAnnotationId, status };
      } else {
        whereClause = "WHERE (status = 0 OR status = 1) AND first_annotation_id = ? ORDER BY annotation_ts DESC";
        paramArray = new Object[] { firstAnnotationId };
      }

      transcriptions = transcriptionDao.select(whereClause, paramArray);

      if (transcriptions != null) {
        // Get users
        LinkedHashMap<Integer, UserAccount> users = new LinkedHashMap<Integer, UserAccount>();
        LinkedHashMap<Integer, Transcription> transcriptionsByUser = new LinkedHashMap<Integer, Transcription>();

        for (Transcription transcription : transcriptions) {
          int accountId = transcription.getAccountId();

          // Get the latest contribution for each user
          if (transcriptionsByUser.containsKey(accountId)) {

            if (transcriptionsByUser.get(accountId).getAnnotationTS()
                .before(transcription.getAnnotationTS())) {
              // Stored transcription's timestamp is older than this one
              transcriptionsByUser.put(accountId, transcription);
            }
          } else {
            transcriptionsByUser.put(accountId, transcription);
          }

          if (!users.containsKey(accountId)) {
            UserAccount user = userAccountDao.select(transcription
                .getAccountId());
            users.put(user.getAccountId(), user);
          }
          resultHashMap.put("TranscriptionsByUser", transcriptionsByUser);
          resultHashMap.put("UserMap", users);
        }

      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      errorCode = TranscriptionErrorCode.INTERNAL_ERROR;
      errorCode
          .setErrorMessage(TranscriptionErrorConstants.internalDatabaseError);
    }

    return new ServiceResponseObject(errorCode, resultHashMap);
  }

  @Override
  public ServiceResponseObject getFullTranscription(String naId, String objectId) {
    return getFullTranscription(naId, objectId, -1);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ServiceResponseObject getFullTranscription(String naId,
      String objectId, int versionNumber) {

    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    UserAccount user = null;
    Transcription mainTranscription = null;

    ServiceResponseObject internalResponse = null;

    // Get lock information if any
    // Retrieve lock information
    internalResponse = lockGetter.getLock(naId, objectId, null);
    AnnotationLockErrorCode lockErrorCode = (AnnotationLockErrorCode) internalResponse
        .getErrorCode();

    // Translate error code
    TranscriptionErrorCode transcriptionErrorCode = TranscriptionErrorCode
        .valueOf(lockErrorCode.toString());

    // Put lock information in results map and get user
    if (transcriptionErrorCode == TranscriptionErrorCode.NONE) {
      AnnotationLock lock = (AnnotationLock) internalResponse.getContentMap()
          .get("AnnotationLock");
      if (lock != null) {
        resultHashMap.put("AnnotationLock", lock);

        try {
          user = userAccountDao.select(lock.getAccountId());
          if (user != null) {
            resultHashMap.put("LockUserAccount", user);
          }

        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          transcriptionErrorCode = TranscriptionErrorCode.INTERNAL_ERROR;
          transcriptionErrorCode
              .setErrorMessage(TranscriptionErrorConstants.internalDatabaseError);
        }
      }
    }

    if (versionNumber == -1) {
      // Retrieve main transcription
      internalResponse = getActiveTranscription(naId, objectId);
    } else {
      internalResponse = getTranscriptionByVersion(naId, objectId,
          versionNumber);
    }

    // Get main transaction and store it in hash map.
    transcriptionErrorCode = (TranscriptionErrorCode) internalResponse
        .getErrorCode();
    if (transcriptionErrorCode == TranscriptionErrorCode.NONE) {
      mainTranscription = (Transcription) internalResponse.getContentMap().get(
          "Transcription");

      resultHashMap.put("Transcription", mainTranscription);

      if (mainTranscription != null) {
        try {
          user = userAccountDao.select(mainTranscription.getAccountId());
          if (user != null) {
            resultHashMap.put("UserAccount", user);
          } else {
            throw new Exception("User not found");
          }

        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          transcriptionErrorCode = TranscriptionErrorCode.INTERNAL_ERROR;
          transcriptionErrorCode
              .setErrorMessage(TranscriptionErrorConstants.internalDatabaseError);
        }
      }

      // Retrieve derived transcriptions
      if (mainTranscription != null) {

        internalResponse = getDerivedTranscriptions(mainTranscription
            .getFirstAnnotationId());

        transcriptionErrorCode = (TranscriptionErrorCode) internalResponse
            .getErrorCode();
      } else {
        // Return not found error
        transcriptionErrorCode = TranscriptionErrorCode.NOT_FOUND;
        transcriptionErrorCode
            .setErrorMessage(TranscriptionErrorConstants.transcriptionNotFound);
      }
    }

    // Put derived transcriptions and user list in results hash map and get lock
    // information
    // If there's no lock retrieve user by transcription
    if (transcriptionErrorCode == TranscriptionErrorCode.NONE) {

      LinkedHashMap<Integer, Transcription> derivedTranscriptions = (LinkedHashMap<Integer, Transcription>) internalResponse
          .getContentMap().get("TranscriptionsByUser");

      if (derivedTranscriptions != null) {
        resultHashMap.put("TranscriptionsByUser", derivedTranscriptions);
      }
      HashMap<Integer, UserAccount> userMap = (HashMap<Integer, UserAccount>) internalResponse
          .getContentMap().get("UserMap");
      if (userMap != null) {
        resultHashMap.put("UserMap", userMap);
      }

    }

    return new ServiceResponseObject(transcriptionErrorCode, resultHashMap);
  }

  @Override
  public ServiceResponseObject getAllTranscriptionVersions(String naId,
      String objectId) {
    HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
    TranscriptionErrorCode transcriptionErrorCode = TranscriptionErrorCode.NONE;

    // Get all transcriptions for the provided values
    try {
      List<AnnotationLogValueObject> logEntries = annotationLogDao.select("TR", naId,
          objectId);

      resultHashMap.put("Versions", logEntries);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      transcriptionErrorCode = TranscriptionErrorCode.INTERNAL_ERROR;
      transcriptionErrorCode
          .setErrorMessage(TranscriptionErrorConstants.internalDatabaseError);
    }

    return new ServiceResponseObject(transcriptionErrorCode, resultHashMap);
  }

}
