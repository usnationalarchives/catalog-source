package gov.nara.opa.common.dataaccess.impl.annotation.translations;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.dataaccess.annotation.translations.TranslationsDao;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.common.valueobject.annotation.translations.TranslationValueObject;
import gov.nara.opa.common.valueobject.annotation.translations.TranslationValueObjectConstants;
import gov.nara.opa.common.valueobject.annotation.translations.TranslationsCollectionValueObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TranslationsJDBCTemplate extends AbstractOpaDbJDBCTemplate
	implements TranslationsDao, TranslationValueObjectConstants {

	@Override
	public void insert(TranslationValueObject translation)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		if (translation.getNaId() != null) {
			translation.getNaId().getBytes(AnnotationConstants.UTF8_ENCODING);
		}
		if (translation.getObjectId() != null) {
			translation.getObjectId().getBytes(AnnotationConstants.UTF8_ENCODING);
		}
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put(ACCOUNT_ID_PARAM, translation.getAccountId());
		inParamMap.put(ANNOTATION_PARAM, translation.getAnnotation());
		inParamMap.put(NAID_PARAM, translation.getNaId());
		inParamMap.put(OBJECT_ID_PARAM, translation.getObjectId());
		inParamMap.put(LANGUAGE_ISO_PARAM, translation.getLanguage());
		inParamMap.put(SAVED_VERS_NUM_PARAM, translation.getSavedVersionNumber());
		inParamMap.put(OPA_ID_PARAM, translation.getOpaId());
		inParamMap.put(PAGE_NUM_PARAM, translation.getPageNum());
		inParamMap.put(ANNOTATION_MD5_PARAM, translation.getAnnotationMD5());
		inParamMap.put(FIRST_ANNOTATION_ID_PARAM, translation.getFirstAnnotationId());
		inParamMap.put(STATUS_PARAM, translation.getStatus());
		int translationId = StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(), "spInsertAnnotationTranslation", inParamMap,
				ANNOTATION_ID_PARAM);
		translation.setAnnotationId(new Integer(translationId));
	}

	@Override
	public boolean update(TranslationValueObject translation)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TranslationsCollectionValueObject select(String naId,
			String objectId, String language) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslationValueObject select(int annotationId)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslationValueObject select(String naId, String objectId,
			String language, int status) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslationValueObject selectByVersion(String naId, String objectId,
			String language, int version) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslationValueObject selectLastOtherUserModifiedTranslation(
			int firstAnnotationId, int accountId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslationValueObject selectLastOwnerModifiedTranslation(
			int firstAnnotationId, int accountId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		// TODO Auto-generated method stub
		return null;
	}

}
