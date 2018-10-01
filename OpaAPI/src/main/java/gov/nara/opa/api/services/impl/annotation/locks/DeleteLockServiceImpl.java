package gov.nara.opa.api.services.impl.annotation.locks;

import gov.nara.opa.api.annotation.locks.AnnotationLockErrorCode;
import gov.nara.opa.api.annotation.locks.AnnotationLockErrorConstants;
import gov.nara.opa.api.dataaccess.annotation.locks.AnnotationLockDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.locks.DeleteLockService;
import gov.nara.opa.architecture.logging.OpaLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DeleteLockServiceImpl implements DeleteLockService {
  
  private static OpaLogger logger = OpaLogger.getLogger(DeleteLockServiceImpl.class);


  @Autowired
  private AnnotationLockDao lockDao;

  @Override
  public ServiceResponseObject delete(String naId, String objectId,
      String languageISO, int accountId) {
    AnnotationLockErrorCode errorCode = AnnotationLockErrorCode.NONE;

    try {
      boolean result = lockDao.delete(naId, objectId, languageISO, accountId);
      if (!result) {
        errorCode = AnnotationLockErrorCode.NOT_FOUND;
        errorCode.setErrorMessage(AnnotationLockErrorConstants.lockNotFound);
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return new ServiceResponseObject(errorCode, null);
  }

}
