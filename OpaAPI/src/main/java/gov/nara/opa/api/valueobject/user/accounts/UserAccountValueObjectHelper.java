package gov.nara.opa.api.valueobject.user.accounts;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.PasswordUtils;
import gov.nara.opa.api.validation.administrator.AdministratorModifyAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorRegisterAccountRequestParameters;
import gov.nara.opa.api.validation.common.accounts.CommonModifyUserAccountRequestParameters;
import gov.nara.opa.api.validation.user.accounts.RegisterAccountRequestParameters;
import gov.nara.opa.api.validation.user.accounts.SetNewPasswordRequestParameters;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAccountValueObjectHelper {

	@Autowired
	private ConfigurationService configService;

	/**
	 * Initializes the internal values for a newly created user account before
	 * inserting it to the database Implements password salting using md5
	 * 
	 * @param userAccount
	 *          The new user account object
	 */

	public UserAccountValueObject createUserAccountForInsert(
			AdministratorRegisterAccountRequestParameters accountRequest) {
		UserAccountValueObject userAccount = new UserAccountValueObject();
		userAccount.setUserName(accountRequest.getUserName());
		userAccount.setFullName(accountRequest.getFullName());
		userAccount.setAccountType(accountRequest.getUserType());
		userAccount.setDisplayFullName(accountRequest.isDisplayFullName());
		userAccount.setEmailAddress(accountRequest.getEmail());
		userAccount.setNaraStaff(userAccount.getEmailAddress().toLowerCase()
				.endsWith(configService.getConfig().getNaraEmail()));
		userAccount.setpWord(accountRequest.getPassword());
		userAccount.setAccountRights(accountRequest.getUserRights());
		PasswordUtils.setSecurityInformation(userAccount);
		userAccount.setpWordChangeId(OPAAuthenticationProvider
				.getAccountIdForLoggedInUser());
		userAccount.setpWordChangeTS(TimestampUtils.getUtcTimestamp());
		userAccount.setAccountCreatedTS(TimestampUtils.getUtcTimestamp());
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
		return userAccount;
	}

	public UserAccountValueObject createUserAccountForInsert(
			RegisterAccountRequestParameters accountRequest) {
		UserAccountValueObject userAccount = new UserAccountValueObject();
		userAccount.setUserName(accountRequest.getUserName());
		userAccount.setFullName(accountRequest.getFullName());
		userAccount.setAccountType(accountRequest.getUserType());
		userAccount.setDisplayFullName(accountRequest.isDisplayFullName());
		userAccount.setEmailAddress(accountRequest.getEmail());
		userAccount.setNaraStaff(userAccount.getEmailAddress().toLowerCase()
				.endsWith(configService.getConfig().getNaraEmail()));
		userAccount.setAccountStatus(false);
		userAccount.setpWord(accountRequest.getPassword());
		userAccount.setAccountRights(accountRequest.getUserRights());

		PasswordUtils.setSecurityInformation(userAccount);
		userAccount.setAccountCreatedTS(new Timestamp((new Date().getTime())));
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
		userAccount.setReferringUrl(accountRequest.getReferringUrl());
		return userAccount;
	}

	public void updateUserAccountAfterActivation(UserAccountValueObject userAccount) {
		userAccount.setAccountStatus(true);
		userAccount.setVerificationGuid(null);
		userAccount.setVerificationTS(null);
		userAccount.setReferringUrl(null);
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
	}

	public void updateUserAccountAfterEmailChange(UserAccountValueObject userAccount) {
		userAccount.setVerificationGuid(null);
		userAccount.setVerificationTS(null);
		userAccount.setEmailAddress(userAccount.getAdditionalEmailAddress());
		userAccount.setNaraStaff(userAccount.getAdditionalEmailAddress().toLowerCase()
				.endsWith(configService.getConfig().getNaraEmail()));
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
		userAccount.setAdditionalEmailAddress(null);
		userAccount.setAdditionalEmailAddressTs(null);
	}

	public void prepareUserAccountForActivationChange(
			UserAccountValueObject userAccount, Boolean status) {
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
		userAccount.setAccountStatus(status);
		userAccount.setAccountNoteFlag(true);
	}

	public void prepareUserAccountForUpdate(UserAccountValueObject userAccount,
			CommonModifyUserAccountRequestParameters requestParameters) {
		String email = requestParameters.getEmail();
		if (!(requestParameters instanceof AdministratorModifyAccountRequestParameters) && email != null) {
			userAccount.setAdditionalEmailAddress(email);
			userAccount.setAdditionalEmailAddressTs(TimestampUtils.getUtcTimestamp());
			userAccount.setVerificationGuid(PasswordUtils.getVerificationCode());
			userAccount.setVerificationTS(PasswordUtils.getVerificationTimestamp());
		} else if ((requestParameters instanceof AdministratorModifyAccountRequestParameters) && email != null) {
			userAccount.setEmailAddress(email);
			userAccount.setNaraStaff(email.toLowerCase()
					.endsWith(configService.getConfig().getNaraEmail()));
		}

		String fullName = requestParameters.getFullName();
		if (fullName != null) {
			userAccount.setFullName(fullName);
		}

		String userType = requestParameters.getUserType();
		if (userType != null) {
			userAccount.setAccountType(userType);
		}

		String userRights = requestParameters.getUserRights();
		if (userType != null) {
			userAccount.setAccountRights(userRights);
		}

		Boolean displayFullName = requestParameters.getDisplayFullName();
		if (displayFullName != null) {
			userAccount.setDisplayFullName(displayFullName);
		}
		Timestamp now = TimestampUtils.getUtcTimestamp();

		String newPassword = requestParameters.getNewPassword();
		if (newPassword != null) {
			userAccount.setpWord(PasswordUtils.saltPassword(newPassword));
			if (requestParameters.getRequestType() == CommonModifyUserAccountRequestParameters.USER_REQUEST) {
				userAccount.setpWordChangeId(userAccount.getAccountId());
			} else if (requestParameters.getRequestType() == CommonModifyUserAccountRequestParameters.ADMIN_REQUEST) {
				userAccount.setpWordChangeId(OPAAuthenticationProvider
						.getAccountIdForLoggedInUser());
			}
			userAccount.setpWordChangeTS(now);
		}
		if (requestParameters.getRequestType() == CommonModifyUserAccountRequestParameters.ADMIN_REQUEST) {
			userAccount.setAccountNoteFlag(true);
		}
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
	}

	public void prepareUserAccountForUpdate(UserAccountValueObject userAccount,
			SetNewPasswordRequestParameters requestParameters) {
		Timestamp now = TimestampUtils.getUtcTimestamp();

		userAccount.setpWord(PasswordUtils.saltPassword(requestParameters
				.getPassword()));
		userAccount.setpWordChangeId(userAccount.getAccountId());
		userAccount.setpWordChangeTS(now);
		userAccount.setVerificationGuid(null);
		userAccount.setVerificationTS(null);
		userAccount.setLastActionTS(now);
	}

	public static UserAccountValueObject getAccountValueObject(UserAccount user) {
		UserAccountValueObject userValueObject = new UserAccountValueObject();
		userValueObject.setAccountId(user.getAccountId());
		userValueObject.setAccountType(user.getAccountType());
		userValueObject.setAccountRights(user.getAccountRights());
		userValueObject.setUserName(user.getUserName());
		userValueObject.setFullName(user.getFullName());
		userValueObject.setDisplayFullName(user.isDisplayNameFlag());
		userValueObject.setEmailAddress(user.getEmailAddress());
		userValueObject.setNaraStaff(user.isNaraStaff());
		userValueObject.setpWord(user.getpWord());
		userValueObject.setLastNotificationId(user.getLastNotificationId());
		if (user.getAccountStatus() == 0) {
			userValueObject.setAccountStatus(false);
		} else {
			userValueObject.setAccountStatus(true);
		}
		userValueObject.setAccountCreatedTS(user.getAccountCreatedTS());
		userValueObject.setpWordChangeId(user.getpWordChangeId());
		userValueObject.setpWordChangeTS(user.getpWordChangeTS());
		userValueObject.setVerificationGuid(user.getVerificationGuid());
		userValueObject.setVerificationTS(user.getVerificationTS());
		userValueObject.setLastActionTS(user.getLastActionTS());
		userValueObject.setAuthenticated(user.isAuthenticated());
		userValueObject.setLoginAttempts(user.getLoginAttempts());
		userValueObject.setLockedOn(user.getLockedOn());
		userValueObject.setFirstInvalidLogin(user.getFirstInvalidLogin());
		return userValueObject;
	}
}
