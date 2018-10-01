package gov.nara.opa.api.dataaccess.user;

import gov.nara.opa.api.validation.administrator.AdministratorSearchAccountsRequestParameters;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.util.List;

/**
 * Data access methods needed for user account administration.
 */
public interface UserAccountDao {

	/**
	 * Verifies if the userName exists in the account table.
	 * 
	 * @param userName
	 *          The value for the user name
	 * @return true of false
	 */
	boolean verifyIfUserNameExists(String userName);

	/**
	 * Verifies if the user email exists in the account table.
	 * 
	 * @param userName
	 *          The value for the user email
	 * @return true of false
	 */
	boolean verifyIfEmailExists(String userName);

	/**
	 * Insert the user account in the account table.
	 * 
	 * @param userAccount
	 *          POJO representing the user account
	 * @return indicates if the insert was successful or not
	 */
	void create(UserAccountValueObject userAccount);

	/**
	 * Create a UserAccountValueObject by selecting it from the database using the
	 * userName
	 * 
	 * @param userName
	 * @return
	 */
	UserAccountValueObject selectByUserName(String userName);

	/**
	 * Create a UserAccountValueObject by selecting it from the database using the
	 * email
	 * 
	 * @param email
	 * @return
	 */
	UserAccountValueObject selectByEmail(String email);

	/**
	 * Create a UserAccountValueObject by selecting it from the database using the
	 * accoumtId
	 * 
	 * @param accountId
	 * @return
	 */
	UserAccountValueObject selectByAccountId(int accountId);

	/**
	 * Retrieves the UserAccountValueObject representation of the provided accountId
	 * @param accountId
	 * @return
	 */
	UserAccountValueObject migrationSelectByTempAccountId(int accountId);

	/**
	 * Retrieves the current password associated with the this userName
	 * 
	 * @param userName
	 * @return
	 */
	String getCurrentPassword(String userName);

	/**
	 * Retrieves the UserAccountValueObject representation which matches the provided activationCode
	 * @param activationCode
	 * @return
	 */
	UserAccountValueObject getUserAccountByActivationCode(String activationCode);

	/**
	 * Updates the user account representation in the database
	 * @param userAccount
	 * 	The userAccount value object 
	 * 	(when lastActionTs is null, this field is not updated in the database)
	 * @return
	 */
	void update(UserAccountValueObject userAccount);

	/**
	 * Deletes the user account representation from the database 
	 * @param userAccount
	 */
	void delete(int accountId);

	/**
	 * Sets login attempts to 0 for the provided user name
	 * @param userName
	 */
	void clearLoginAttemptsInfo(String userName);

	/**
	 * Updates login attempts for the provided user name
	 * @param userName
	 * @param lockAccount
	 * @param loginAttempts
	 */
	void updateLoginAttemptsInfo(String userName, boolean lockAccount,
			int loginAttempts);

	/**
	 * Updates deactivation warning field for the provided UserAccountValueObject
	 * @param userAccount
	 */
	void updateDeactivationWarning(UserAccountValueObject userAccount);

	/**
	 * Retrieves a list of UserAccountValueObject which match the provided parameters
	 * @param requestParameters
	 * @return
	 */
	List<UserAccountValueObject> search(
			AdministratorSearchAccountsRequestParameters requestParameters);

	/**
	 * Returns the total number of UserAccountValueObject found during a search
	 * @param requestParameters
	 * @return
	 */
	int getSearchTotalResults(AdministratorSearchAccountsRequestParameters requestParameters);

	/**
	 * 
	 * @return
	 */
	int getTotalUserCount();

}
