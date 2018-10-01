package gov.nara.opa.api.services.user.accounts;

public interface AccountMaintenanceService {

	/**
	 * Removes idle accounts
	 * 	Idle accounts are users who haven't used their account
	 * 	for a some time
	 * @return The number of idle accounts that were disabled
	 */
	public int disableIdleAccounts() throws Exception;

	/**
	 * Removes unverified accounts
	 * 	Unverified accounts are users who haven't verify their
	 * 	email addresses after a registration 
	 * @return The number of unverified accounts that were removed
	 */
	public int removeUnverifiedAccounts() throws Exception;

	/**
	 * Remove unverified email changes
	 * 	Unverified email changes are email change that the user who
	 * 	requested them hasn't verified
	 * @return The number of unverified email changes that were removed
	 */
	public int removeUnverifiedEmailChanges() throws Exception;
	
	
	/**
	 * Removes expired exports
	 *  Expired exports are identified by their expiration date when created.
	 * @return The number of removed exports
	 * @throws Exception
	 */
	public int removeExpiredExports() throws Exception;

}
