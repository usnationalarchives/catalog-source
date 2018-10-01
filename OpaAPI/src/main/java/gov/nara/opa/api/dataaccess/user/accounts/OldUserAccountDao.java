package gov.nara.opa.api.dataaccess.user.accounts;

import gov.nara.opa.api.user.accounts.UserAccount;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface OldUserAccountDao {

  /**
   * Gets a user account by Id
   * 
   * @param userId
   *          The id of the accounts to retrieve
   * @return The user account that matches the provided Id, or null if none was
   *         found.
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  UserAccount select(int userId) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Gets a list of user accounts that match the values in the list of params.
   * It builds a where clause whose elements are joined by ANDs.
   * 
   * @param params
   *          The filtering parameters
   * @return A list of user accounts that match the parameters in the list
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  List<UserAccount> select(LinkedHashMap<String, byte[]> params)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException;

  /**
   * Creates a record in the accounts table for the provided user account
   * 
   * @param userAccount
   *          The user account instance to persist in the database
   * @return True if the operation was successful, false otherwise
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  boolean create(UserAccount userAccount) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Updates a user account record in the database with the values in the
   * provided instance
   * 
   * @param userAccount
   *          The source user account instance to persist
   * @return True if the operation was successful, false otherwise
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  boolean update(UserAccount userAccount) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

}
