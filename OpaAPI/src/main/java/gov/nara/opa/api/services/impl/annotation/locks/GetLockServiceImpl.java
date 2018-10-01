package gov.nara.opa.api.services.impl.annotation.locks;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.locks.AnnotationLockErrorCode;
import gov.nara.opa.api.dataaccess.annotation.locks.AnnotationLockDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.locks.GetLockService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class GetLockServiceImpl implements GetLockService {
  
  private static OpaLogger logger = OpaLogger.getLogger(GetLockServiceImpl.class);
  
  @Autowired
  private AnnotationLockDao dao;

  @Autowired
  private ConfigurationService configurationService;

  private int lockActivityMinutes;
  
  @Override
  public ServiceResponseObject getLock(String naId, String objectId,
      String languageISO) {
    AnnotationLockErrorCode errorCode = AnnotationLockErrorCode.NONE;
    HashMap<String, Object> resultsMap = new HashMap<String, Object>();

    try {
      lockActivityMinutes = configurationService.getConfig().getTranscriptionInactivityTime();
      
      List<AnnotationLock> locks = dao.getLocks(naId, objectId, languageISO,
          lockActivityMinutes);
      if (locks != null && locks.size() > 0) {
        resultsMap.put("AnnotationLock", locks.get(0));
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    return new ServiceResponseObject(errorCode, resultsMap);
  }

}
