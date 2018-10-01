package gov.nara.opa.api.services.annotation.translations;

import gov.nara.opa.common.valueobject.annotation.translations.TranslationValueObject;
import gov.nara.opa.common.valueobject.annotation.translations.TranslationsCollectionValueObject;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface TranslationService {

	public TranslationValueObject saveAndRelock(TranslationValueObject translation) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException;

	public TranslationValueObject saveAndUnlock(TranslationValueObject translation) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException;

	public boolean updateTranslation(TranslationValueObject translation) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException;

	public TranslationsCollectionValueObject getTranslations(String naId, String objectId, String language)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	public TranslationValueObject getActiveTranslation(String naId, String objectId, 
			String language);

	public TranslationValueObject getTranslationByVersion(String naId, String objectId, 
			String language, int version);

	public TranslationValueObject getTranslationById(int annotationId) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException;

	public TranslationValueObject getTranslationByStatus(String naId, String objectId, 
			String language, int status);

	public TranslationValueObject getLastOtherUserModifiedTranslation(int firstAnnotationId, 
			int accountId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	public TranslationValueObject getLastOwnerModifiedTranslation(int firstAnnotationId,
		      int accountId) throws DataAccessException,
		      UnsupportedEncodingException, BadSqlGrammarException;

}
