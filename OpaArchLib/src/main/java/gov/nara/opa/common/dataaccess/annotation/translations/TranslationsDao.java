package gov.nara.opa.common.dataaccess.annotation.translations;

import gov.nara.opa.common.valueobject.annotation.translations.TranslationValueObject;
import gov.nara.opa.common.valueobject.annotation.translations.TranslationsCollectionValueObject;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface TranslationsDao {

	/**
	 * Inserts a new translation in the database
	 * @param translation
	 * 		TranslationValueObject instance to be inserted in the database
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	void insert(TranslationValueObject translation) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Updates a translation in the database
	 * @param translation
	 * 		TranslationValueObject instance to be updated in the database
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	boolean update(TranslationValueObject translation) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Get a TranslationValueObject with the supplied naId, objectId and languages
	 * @param naId
	 * 		naId to filter on
	 * @param objectId
	 * 		objectId to filter on
	 * @param language
	 * 		language to filter on
	 * @return
	 * 		a list of TranslationValueObject associated with the supplied
	 * 		naId, objectId and language
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	TranslationsCollectionValueObject select(String naId, String objectId, String language)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Gets a translation with the provided annotationId
	 * @param annotationId
	 * 		annotationId to filter on
	 * @return
	 * 		TranslationValueObject for the provided annotationId
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	TranslationValueObject select(int annotationId) throws DataAccessException,
    UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Gets the current active translation based on the supplied parameters
	 * @param naId
	 * 		naId to filter on
	 * @param objectId
	 * 		objectId to filter on
	 * @param language
	 * 		language to filter on
	 * @param status
	 * 		status to filter on
	 * @return
	 * 		a TranslationValueObject instance containing the current active translation
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	TranslationValueObject select(String naId, String objectId, 
			String language, int status)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Gets a translation based on the version number provided
	 * @param naId
	 * 		naId to filter on
	 * @param objectId
	 * 		objectId to filter on
	 * @param language
	 * 		language to filter on
	 * @param version
	 * 		version number to filter on
	 * @return
	 * 		a TranslationValueObject instance with the provided version number
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	TranslationValueObject selectByVersion(String naId, String objectId, 
			String language, int version)
		      throws DataAccessException, UnsupportedEncodingException,
		      BadSqlGrammarException;

	/**
	 * 
	 * @param firstAnnotationId
	 * @param accountId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	TranslationValueObject selectLastOtherUserModifiedTranslation(int firstAnnotationId, 
			int accountId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * 
	 * @param firstAnnotationId
	 * @param accountId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	TranslationValueObject selectLastOwnerModifiedTranslation(int firstAnnotationId,
		      int accountId) throws DataAccessException,
		      UnsupportedEncodingException, BadSqlGrammarException;
}
