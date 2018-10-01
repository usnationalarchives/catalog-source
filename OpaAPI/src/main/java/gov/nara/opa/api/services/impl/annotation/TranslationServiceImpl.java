package gov.nara.opa.api.services.impl.annotation;

import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.services.annotation.locks.AnnotationLockService;
import gov.nara.opa.api.services.annotation.translations.TranslationService;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.dataaccess.annotation.translations.TranslationsDao;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.translations.TranslationValueObject;
import gov.nara.opa.common.valueobject.annotation.translations.TranslationsCollectionValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TranslationServiceImpl implements TranslationService {

	private static OpaLogger logger = OpaLogger.getLogger(TranslationServiceImpl.class);

	@Autowired
	AnnotationLockService lockService;

	@Autowired
	TranslationsDao translationDao;

	@Autowired
	AnnotationLogDao annotationLogDao;

	public TranslationValueObject saveAndRelock(TranslationValueObject translation) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException {
		return save(translation, false);
	}

	public TranslationValueObject saveAndUnlock(TranslationValueObject translation) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException {
		return save(translation, true);
	}

	private TranslationValueObject save(
			TranslationValueObject translation, boolean unlock) {
		TranslationValueObject translationCreated = null;
		return translationCreated;
	}

	@Override
	public boolean updateTranslation(TranslationValueObject translation)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TranslationsCollectionValueObject getTranslations(String naId,
			String objectId, String language) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		TranslationsCollectionValueObject translations = null;
		try {
			translations = translationDao.select(naId, objectId, language);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return translations;
	}

	@Override
	public TranslationValueObject getActiveTranslation(String naId,
			String objectId, String language) {
		TranslationValueObject translation = null;
		try {
			translation = translationDao.select(naId, objectId, language, 1);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return translation;
	}

	@Override
	public TranslationValueObject getTranslationByVersion(String naId,
			String objectId, String language, int version) {
		TranslationValueObject translation = null;
		AnnotationLogValueObject log = null;
		try {
			log = annotationLogDao.selectByVersion(
					AnnotationConstants.ANNOTATION_TYPE_TRANSLATION, naId, objectId, version);
			if(log != null) {
		        int translationId = log.getAnnotationId();
		        translation = translationDao.select(translationId);
		      }
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return translation;
	}

	@Override
	public TranslationValueObject getTranslationById(int annotationId)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		TranslationValueObject translation = null;
		try {
			translation = translationDao.select(annotationId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return translation;
	}

	@Override
	public TranslationValueObject getTranslationByStatus(String naId,
			String objectId, String language, int status) {
		TranslationValueObject translation = null;
		
		return translation;
	}

	@Override
	public TranslationValueObject getLastOtherUserModifiedTranslation(
			int firstAnnotationId, int accountId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		TranslationValueObject translation = null;
		try {
			translation = translationDao.
					selectLastOtherUserModifiedTranslation(firstAnnotationId, accountId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return translation;
	}

	@Override
	public TranslationValueObject getLastOwnerModifiedTranslation(
			int firstAnnotationId, int accountId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		TranslationValueObject translation = null;
		try {
			translation = translationDao.
					selectLastOwnerModifiedTranslation(firstAnnotationId, accountId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return translation;
	}

}
