package gov.nara.opa.api.dataaccess.annotation.locks;

import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface AnnotationLockValueObjectDao {
	
	/**
	 * Inserts an annotation lock in the database
	 * @param annotationLock
	 * 		The lock instance
	 * @return true if annotationLock was successfully inserted, false otherwise.
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	boolean insert(AnnotationLockValueObject annotationLock) throws DataAccessException,
		UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Updates an annotation lock in the database
	 * @param annotationLock
	 * 		The lock instance
	 * @return true if annotationLock was successfully inserted, false otherwise.
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	boolean update(AnnotationLockValueObject annotationLock) throws DataAccessException,
		UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Determines if there's an active lock for the provided values 
	 * @param accountId
	 * 		User id who validates lock
	 * @param naId
	 * 		NaId for the item to lock
	 * @param objectId
	 * 		ObjectId for the item to lock
	 * @param languageISO
	 * 		ISO code for the language
	 * @param minuteWindow
	 * 		Expiration minute window
	 * @return
	 * 		true if lock is valid based on parameters, false otherwise.
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	boolean validateLock(int accountId, String naId, String objectId,
		      String languageISO, int minuteWindow) throws DataAccessException,
		      UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Gets all active locks for the provided values
	 * @param naId
	 * 		NaId for the item to get locks form
	 * @param objectId
	 * 		ObjectId for the item to get locks from
	 * @param languageISO
	 * 		ISO code for the language to get locks from
	 * @param minuteWindow
	 * 		Expiration minute window
	 * @return
	 * 		a list of active locks for the provided values
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	List<AnnotationLockValueObject> getLocks(String naId, String objectId,
		      String languageISO, int minuteWindow) throws DataAccessException,
		      UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Retrieves all active locks for the provided user Id
	 * @param accountId
	 * 		User account id for retrieving active locks
	 * @param minuteWindow
	 * 		Expiration minute window
	 * @return
	 * 		a list of active locks for the provided accountId
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	List<AnnotationLockValueObject> getLocksForUser(int accountId, int minuteWindow)
		      throws DataAccessException, UnsupportedEncodingException,
		      BadSqlGrammarException;

	/**
	 * Retrieves a lock by its id
	 * @param lockId
	 * 		Id for the lock to retrieve
	 * @return
	 * 		an AnnotationLockValueObject for the provided id
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	AnnotationLockValueObject getLock(int lockId) 
			throws DataAccessException, UnsupportedEncodingException, 
			BadSqlGrammarException;

	/**
	 * Deletes a specific lock for the provided values
	 * @param naId
	 * 		NaId for the item to delete locks form
	 * @param objectId
	 * 		ObjectId for the item to delete locks from
	 * @param languageISO
	 * 		ISO code for the language to delete locks from
	 * @param accountId
	 * 		User account id for deleting active locks
	 * @return
	 * 		true if deletion was successful, false otherwise
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	boolean delete(String naId, String objectId, String languageISO, int accountId)
		      throws DataAccessException, UnsupportedEncodingException,
		      BadSqlGrammarException;

	/**
	 * Removes all expired locks for the provided minute window
	 * @param minuteWindow
	 * 		Expiration minute window
	 * @return
	 * 		number of deleted expired locks
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	int clearExpiredLocks(int minuteWindow) 
			throws DataAccessException, UnsupportedEncodingException, 
			BadSqlGrammarException;
}