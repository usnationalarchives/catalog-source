package gov.nara.opa.api.dataaccess.annotation.logs;

import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface AnnotationLogDao {

	/**
	 * Inserts a new annotation log
	 * 
	 * @param annotationLog
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public boolean insert(AnnotationLogValueObject annotationLog)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Updates an annotation log
	 * 
	 * @param annotationLog
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public boolean update(AnnotationLogValueObject annotationLog)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Gets the annotation log for the provided Id
	 * 
	 * @param logId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public AnnotationLogValueObject select(int logId)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Get all log entries for a given annotation type and annotation Id sorted
	 * by version descending
	 * 
	 * @param annotationType
	 * @param annotationId
	 * @return
	 */
	public List<AnnotationLogValueObject> select(String annotationType,
			int annotationId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Gets all annotation logs for the provided parameters
	 * 
	 * @param annotationType
	 * @param naId
	 * @param objectId
	 * @param status
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<AnnotationLogValueObject> select(String annotationType,
			String naId, String objectId, boolean status)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Gets all annotation logs for the provided parameters
	 * 
	 * @param annotationType
	 * @param naId
	 * @param objectId
	 * @param status
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<AnnotationLogValueObject> select(String annotationType,
			String naId, String objectId, String language, boolean status)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Gets all annotation logs for the provided parameters
	 * 
	 * @param annotationType
	 * @param naId
	 * @param objectId
	 * @param parentId
	 * @param status
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<AnnotationLogValueObject> select(String annotationType,
			String naId, String objectId, Integer parentId, boolean status)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Gets all annotation logs for an annotation type and item identifiers
	 * 
	 * @param annotationType
	 * @param naId
	 * @param objectId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<AnnotationLogValueObject> select(String annotationType,
			String naId, String objectId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Gets an annotation of the requested type by it's version
	 * 
	 * @param annotationType
	 * @param naId
	 * @param objectId
	 * @param version
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public AnnotationLogValueObject selectByVersion(String annotationType,
			String naId, String objectId, int version)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Retrieve the highest existing version of an annotation of 0 if none found
	 * 
	 * @param annotationType
	 * @param naId
	 * @param objectId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public int getHighestVersion(String annotationType, String naId,
			String objectId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Disables a log entry by annotationId
	 * 
	 * @param annotationId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	int disableByAnnotationId(Integer annotationId, String annotationType);

	/**
	 * Disables a log entry by log Id
	 * 
	 * @param logId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public int disableByLogId(int logId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;
}
