package gov.nara.opa.api.dataaccess.annotation.locks;

import gov.nara.opa.api.annotation.locks.AnnotationLock;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface AnnotationLockDao {

  /**
   * Inserts a lock in the database
   * 
   * @param annotationLock
   *          The lock instance
   * @return True if creation was successful
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  boolean insert(AnnotationLock annotationLock) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Updates an existing annotation lock
   * 
   * @param annotationLock
   *          The annotation lock instance
   * @return True if the update was performed successfuly
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  boolean update(AnnotationLock annotationLock) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Determines if there's an active lock for the provided values
   * 
   * @param accountId
   *          The user account Id
   * @param naId
   *          The item Id
   * @param objectId
   *          The digital object Id
   * @param languageISO
   *          The standard language ISO
   * @param minuteWindow
   *          The expiration minute window
   * @return True if an active lock was found
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  boolean validateLock(int accountId, String naId, String objectId,
      String languageISO, int minuteWindow) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Gets all active locks for the provided values
   * 
   * @param naId
   *          The item Id
   * @param objectId
   *          The digital object Id
   * @param languageISO
   *          The language ISO value
   * @param minuteWindow
   *          The expiration minute window
   * @return The list of active locks for the provided values
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  List<AnnotationLock> getLocks(String naId, String objectId,
      String languageISO, int minuteWindow) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Retrieves all active locks for the provided user Id
   * 
   * @param accountId
   *          The user Id
   * @param minuteWindow
   *          The expiration minute window
   * @return The active locks for the user
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  List<AnnotationLock> getLocksForUser(int accountId, int minuteWindow)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException;

  /**
   * Gets a lock by Id
   * 
   * @param lockId
   *          The requested lock id
   * @return A valid annotation lock instance
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  AnnotationLock getLock(int lockId) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Deletes a specific lock for the provided values
   * 
   * @param naId
   *          The item Id
   * @param objectId
   *          The digital object Id
   * @param languageISO
   *          The language ISO value
   * @param accountId
   *          The user account that performs the unlock
   * @return True if deletion was successful
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  boolean delete(String naId, String objectId, String languageISO, int accountId)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException;

  /**
   * Removes all expired locks for the provided minute window
   * 
   * @param minuteWindow
   *          The expiration window
   * @return The number of deleted locks
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  int clearExpiredLocks(int minuteWindow) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

}
