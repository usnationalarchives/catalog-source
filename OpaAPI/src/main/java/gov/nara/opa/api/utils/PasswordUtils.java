package gov.nara.opa.api.utils;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utility class for password handling
 */
public class PasswordUtils {
	private static OpaLogger logger = OpaLogger.getLogger(PasswordUtils.class);

	private static SecureRandom random;

	private static SecureRandom getRandom() {
		if (random == null) {
			try {
				random = SecureRandom.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException ex) {
				logger.error(ex.getMessage(), ex);
				//throw new OpaRuntimeException(ex);
			}
		}
		return random;
	}

	/**
	 * Performs salting on a password field
	 * 
	 * @param pwd
	 *          Password value to be salted
	 * @return Salted password value
	 */
	public static String saltPassword(String pwd) {
		//Get random number for salt
		Integer saltNumber = getRandom().nextInt();

		//Get salt string
		String salt = DigestUtils.md5Hex(saltNumber.toString());

		//Salt password
		String saltedPwd = DigestUtils.md5Hex(pwd + salt);

		//Return salted password plus salt in a 62 char string
		return saltedPwd + salt;
	}

	/**
	 * Method to check if two password values are equal
	 * 
	 * @param storedPasswordValue
	 *          The salted password plus the salt string from the stored user
	 * @param providedPasswordValue
	 *          The password to match
	 * @return password matches (true/false
	 */
	public static boolean passwordMatches(String storedPasswordValue,
			String providedPasswordValue) {
		//Get salt string from stored value
		String saltString = storedPasswordValue.substring(32);

		//Get the actual salted password
		String saltedPassword = storedPasswordValue.substring(0, 32);

		//Salt the provided password with the extracted salt
		String saltedProvidedPassword = DigestUtils.md5Hex(providedPasswordValue
				+ saltString);

		//Compare salted provided password with stored salted password
		return saltedPassword.equals(saltedProvidedPassword);
	}

	/**
	 * Gets a random UUID for verification codes
	 */
	public static String getVerificationCode() {
		// Verification information
		return UUID.randomUUID().toString();
	}

	/**
	 * Gets a timestamp
	 */
	public static Timestamp getVerificationTimestamp() {
		return TimestampUtils.getTimestamp();
	}

	public static void setSecurityInformation(UserAccountValueObject userAccount) {
		// Password salting
		userAccount.setpWord(PasswordUtils.saltPassword(userAccount.getpWord()));
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
		updateVerificationGuid(userAccount);
		updateVerificationTimestamp(userAccount);
	}

	public static void updateVerificationGuid(UserAccountValueObject userAccount) {
		userAccount.setVerificationGuid(PasswordUtils.getVerificationCode());    
	}

	public static void updateVerificationTimestamp(UserAccountValueObject userAccount) {
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
		userAccount.setVerificationTS(PasswordUtils.getVerificationTimestamp());    
	}

}
