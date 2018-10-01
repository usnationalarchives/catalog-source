package gov.nara.opa.api.services.impl.annotation.locks;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.locks.AnnotationLockErrorCode;
import gov.nara.opa.api.annotation.locks.AnnotationLockErrorConstants;
import gov.nara.opa.api.dataaccess.annotation.locks.AnnotationLockDao;
import gov.nara.opa.api.dataaccess.user.accounts.OldUserAccountDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.locks.CreateLockService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CreateLockServiceImpl implements CreateLockService {
  private static OpaLogger logger = OpaLogger.getLogger(CreateLockServiceImpl.class);
  
  @Autowired
  private AnnotationLockDao lockDao;

  @Autowired
  private OldUserAccountDao userDao;
  
  @Autowired
  private ConfigurationService configurationService;

  private int lockActivityMinutes;

  @Override
  public ServiceResponseObject create(AnnotationLock annotationLock) {
    AnnotationLockErrorCode errorCode = AnnotationLockErrorCode.NONE;
    AnnotationLock resultLock = null;
    UserAccount resultUser = null;
    

    try {
      lockActivityMinutes = configurationService.getConfig().getTranscriptionInactivityTime();

      // Get existing locks
      List<AnnotationLock> locks = lockDao.getLocks(annotationLock.getNaId(),
          annotationLock.getObjectId(), annotationLock.getLanguageISO(),
          lockActivityMinutes);

      boolean okToCreate = true;
      if (locks.size() > 0) {
        // Evaluate if locks belong to a different user
        for (AnnotationLock lock : locks) {
          if (lock.getAccountId() != annotationLock.getAccountId()) {
            okToCreate = false;
          }
        }
      }

      if (okToCreate) {

        boolean result = false;
        if (locks.size() > 0) {
          // If lock exists for this user, perform update
          resultLock = locks.get(0);
          result = lockDao.update(resultLock);
        } else {
          // Perform insert
          result = lockDao.insert(annotationLock);
        }

        // Evaluate operation results
        if (result) {
          // Get lock
          locks = lockDao.getLocks(annotationLock.getNaId(),
              annotationLock.getObjectId(), annotationLock.getLanguageISO(),
              lockActivityMinutes);

          resultUser = userDao.select(annotationLock.getAccountId());

          // Return lock instance
          if (locks != null && !locks.isEmpty()) {
            resultLock = locks.get(0);
          } else {
            errorCode = AnnotationLockErrorCode.INTERNAL_ERROR;
            errorCode
                .setErrorMessage(AnnotationLockErrorConstants.internalDatabaseError);
          }
        } else {
          // Internal error
          errorCode = AnnotationLockErrorCode.INTERNAL_ERROR;
          errorCode
              .setErrorMessage(AnnotationLockErrorConstants.internalDatabaseError);
        }
      } else {

        // Item is already locked
        resultLock = locks.get(0);

        resultUser = userDao.select(annotationLock.getAccountId());

        // Return lock instance
        if (locks != null && !locks.isEmpty()) {
          resultLock = locks.get(0);
        }

        errorCode = AnnotationLockErrorCode.LOCKED_BY_ANOTHER;
        errorCode.setErrorMessage(AnnotationLockErrorConstants.alreadyLocked);
      }

    } catch (DataIntegrityViolationException e) {
      logger.error(e.getMessage(), e);
      errorCode = AnnotationLockErrorCode.INTERNAL_ERROR;
      errorCode
          .setErrorMessage(AnnotationLockErrorConstants.userAccountNotFound);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      errorCode = AnnotationLockErrorCode.INTERNAL_ERROR;
      errorCode.setErrorMessage(e.getMessage());
    }

    HashMap<String, Object> results = new HashMap<String, Object>();
    results.put("AnnotationLock", resultLock);
    results.put("UserAccount", resultUser);

    return new ServiceResponseObject(errorCode, results);
  }


}
