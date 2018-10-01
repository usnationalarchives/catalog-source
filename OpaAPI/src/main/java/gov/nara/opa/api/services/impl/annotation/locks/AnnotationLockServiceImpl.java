package gov.nara.opa.api.services.impl.annotation.locks;

import gov.nara.opa.api.dataaccess.annotation.locks.AnnotationLockValueObjectDao;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.annotation.locks.AnnotationLockService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.annotation.locks.AnnotationLockLanguageRequestParameters;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AnnotationLockServiceImpl implements AnnotationLockService {

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private AnnotationLockValueObjectDao lockDao;

	@Autowired
	private AnnotationLockServiceHelper lockHelper;

	@Autowired
	private UserAccountDao userDao;

	private int lockActivityMinutes;

	private static OpaLogger logger = OpaLogger.getLogger(AnnotationLockServiceImpl.class);

	@Override
	public AnnotationLockValueObject create(
			AnnotationLockLanguageRequestParameters annotationLockRequestParameters) 
					throws OpaRuntimeException {

		UserAccountValueObject user = OPAAuthenticationProvider.
				getAccountValueObjectForLoggedInUser();
		AnnotationLockValueObject resultLock = null;

		try {
		      lockActivityMinutes = configurationService.getConfig().
		    		  getTranscriptionInactivityTime();
		      List<AnnotationLockValueObject> locks = lockDao.getLocks(
		    		  annotationLockRequestParameters.getNaId(),
		    		  annotationLockRequestParameters.getObjectId(), 
		    		  annotationLockRequestParameters.getLanguage(),
		              lockActivityMinutes);

		      boolean okToCreate = true;
		      if (locks.size() > 0) {
		        // Evaluate if locks belong to a different user
		        for (AnnotationLockValueObject lock : locks) {
		          if (lock.getAccountId() != user.getAccountId()) {
		            okToCreate = false;
		          }
		        }
		      }
		      if (okToCreate) {
		    	  AnnotationLockValueObject annotationLock = lockHelper.
		    			  createAnnotationLockValueObject(user.getAccountId(), 
		    					  annotationLockRequestParameters.getNaId(), 
		    					  annotationLockRequestParameters.getObjectId(), 
		    					  annotationLockRequestParameters.getLanguage());
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
		        	  locks = lockDao.getLocks(annotationLockRequestParameters.getNaId(),
		            		annotationLockRequestParameters.getObjectId(), 
		            		annotationLockRequestParameters.getLanguage(),
		                lockActivityMinutes);

		        	  // Return lock instance
		        	  if (locks != null && !locks.isEmpty()) {
		        		  resultLock = locks.get(0);
		        	  } else {
		        		  throw new OpaRuntimeException(
		        				  ErrorConstants.internalDatabaseError);
		        	  }
		          } else {
		        	  throw new OpaRuntimeException(
		        			  ErrorConstants.internalDatabaseError);
		          }
		      } else {
		    	// Item is already locked
		          resultLock = locks.get(0);

		          // Return lock instance
		          if (locks != null && !locks.isEmpty()) {
		            resultLock = locks.get(0);
		          }
		        }
		} catch (DataIntegrityViolationException e) {
			throw new OpaRuntimeException(ErrorConstants.USER_NAME_NOT_EXISTS);
		} catch (Exception e) {
			throw new OpaRuntimeException(e.getMessage());
		}

		return resultLock;
	}

	@Override
	public AnnotationLockValueObject getLock(String naId, String objectId,
			String languageISO) {

		try {
			lockActivityMinutes = configurationService.getConfig().
					getTranscriptionInactivityTime();

			List<AnnotationLockValueObject> locks = lockDao.getLocks(naId,
					objectId, languageISO, lockActivityMinutes);
			if (locks != null && locks.size() > 0) {
				return locks.get(0);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	@Override
	public boolean delete(AnnotationLockLanguageRequestParameters annotationLock)
			throws OpaRuntimeException {
		return delete(annotationLock.getNaId(), annotationLock.getObjectId(),
				annotationLock.getLanguage(), OPAAuthenticationProvider.
				getAccountValueObjectForLoggedInUser().getAccountId());
	}

	@Override
	public boolean delete(String naId, String objectId,
			String languageISO, int accountId) {
		boolean result = false;
		try {
			result = lockDao.delete(naId, objectId, languageISO, accountId);
	    } catch (Exception e) {
	    	logger.error(e.getMessage(), e);
	    }
		return result;
	}

	@Override
	public boolean validateLock(int accountId, String naId, String objectId,
			String languageISO) {

		boolean result = false;

		try {
			lockActivityMinutes = configurationService.getConfig().
					getTranscriptionInactivityTime();
			result = lockDao.validateLock(accountId, naId, objectId, 
					languageISO, lockActivityMinutes);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return result;
	}

	@Override
	public boolean canLock(String naId, String objectId, String languageISO) {

		boolean result = true;

		try {
			lockActivityMinutes = configurationService.getConfig().
					getTranscriptionInactivityTime();
			List<AnnotationLockValueObject> locks = lockDao.
					getLocks(naId, objectId, languageISO, lockActivityMinutes);
			if (locks != null && locks.size() > 0) {
				result = false;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return result;
	}

}