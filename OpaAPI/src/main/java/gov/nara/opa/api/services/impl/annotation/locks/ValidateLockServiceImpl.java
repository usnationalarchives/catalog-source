package gov.nara.opa.api.services.impl.annotation.locks;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.dataaccess.annotation.locks.AnnotationLockDao;
import gov.nara.opa.api.services.annotation.locks.ValidateLockService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ValidateLockServiceImpl implements ValidateLockService {
  
  private static OpaLogger logger = OpaLogger.getLogger(ValidateLockServiceImpl.class);
  
  @Autowired
  private AnnotationLockDao dao;

  @Autowired
  private ConfigurationService configurationService;

  private int lockActivityMinutes;
  
  @Override
  public boolean validateLock(int accountId, String naId, String objectId,
      String languageISO) {
    boolean result = false;

    try {
      lockActivityMinutes = configurationService.getConfig().getTranscriptionInactivityTime();
      
      result = dao.validateLock(accountId, naId, objectId, languageISO,
          lockActivityMinutes);
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    return result;
  }

  @Override
  public boolean canLock(String naId, String objectId, String languageISO) {
    boolean result = true;

    try {
      lockActivityMinutes = configurationService.getConfig().getTranscriptionInactivityTime();
      
      List<AnnotationLock> locks = dao.getLocks(naId, objectId, languageISO,
          lockActivityMinutes);
      if (locks != null && locks.size() > 0) {
        result = false;
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    return result;
  }

}
