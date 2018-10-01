package gov.nara.opa.api.user.accounts.email;

import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.EmailClient;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper class for user account emails
 */
@Component
public class UserAccountEmailHelper {
	private static OpaLogger log = OpaLogger
			.getLogger(UserAccountEmailHelper.class);

	@Autowired
	private EmailClient emailClient;

	@Value("${currentApi}")
	private String currentApi;

	@Value("${activationServer}")
	private String activationServer;

	@Value("${activationEmailLink}")
	private String activationEmailLink;

	@Value("${activationAdminEmailLink}")
	private String activationAdminEmailLink;

	@Value("${activationReturnUrlParams}")
	private String activationReturnUrlParams;

	@Value("${activationEmailSubject}")
	private String activationEmailSubject;

	@Value("${emailVerificationSubject}")
	private String emailVerificationSubject;

	@Value("${verifyEmailLink}")
	private String verifyEmailLink;

	@Value("${activationFailedEmailBody}")
	private String activationFailedEmailBody;

	@Value("${activationFailedEmailSubject}")
	private String activationFailedEmailSubject;

	@Value("${userNameRecoveryEmailBody}")
	private String userNameRecoveryEmailBody;

	@Value("${userNameRecoveryEmailSubject}")
	private String userNameRecoveryEmailSubject;

	@Value("${passwordResetEmailLink}")
	private String passwordResetEmailLink;

	@Value("${passwordResetEmailSubject}")
	private String passwordResetEmailSubject;

	@Value("${passwordResetFailedEmailBody}")
	private String passwordResetFailedEmailBody;

	@Value("${passwordResetFailedEmailSubject}")
	private String passwordResetFailedEmailSubject;

	@Value("${accountDeactivationSubject}")
	private String accountDeactivationSubject;

	@Value("${deactivationWarningSubject}")
	private String deactivationWarningSubject;

	@Value("${supportEmailAddress}")
	private String supportEmailAddress;

	@Value("${accountManagementAddress}")
	private String accountManagementAddress;

	@Value("${emailNaraLogoPath}")
	private String emailNaraLogoPath;

	@Value("${imageTag}")
	private String imageTag;

	@Value("${sendEmailsTo_naratest-}")
	private boolean sendEmailsToNaraTest;

	public static final String NARA_TEST_EMAIL_BEGINS_WITH = "naratest-";

	/**
	 * Sends out an email with the activation failure content
	 * 
	 * @param userAccount
	 */
	public void sendEmailVerificationFailure(UserAccount userAccount) {
		String subject = activationFailedEmailSubject;
		String body = activationFailedEmailBody;
		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Sends out the email verification message with the verification link
	 * 
	 * @param userAccount
	 */
	public void sendRegisterVerification(UserAccountValueObject userAccount) {
		sendRegisterVerification(userAccount, false);
	}

	public void sendRegisterVerification(UserAccountValueObject userAccount, String returnUrl, String returnText) {
		sendRegisterVerification(userAccount, false, returnUrl, returnUrl);
	}

	public void sendRegisterVerification(UserAccountValueObject userAccount, boolean isAdmin) {
		sendRegisterVerification(userAccount, false, null, null);
	}

	public void sendRegisterVerification(UserAccountValueObject userAccount, boolean isAdmin, String returnUrl, String returnText) {
		String emailBody = UserAccountEmailBody.registrationBody;

		String subject = activationEmailSubject;

		String activationLink;
		if(isAdmin) {
			String returnUrlParams = "";
			try {
				returnUrlParams = (!StringUtils.isNullOrEmtpy(returnUrl) && !StringUtils.isNullOrEmtpy(returnText) 
						? String.format(activationReturnUrlParams, URLEncoder.encode(returnUrl, "UTF-8"), 
								URLEncoder.encode(returnText, "UTF-8")) 
								: "");
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage(), e);
				throw new OpaRuntimeException(e);
			}

			activationLink = String.format(activationAdminEmailLink,
					activationServer, userAccount.getVerificationGuid(), currentApi,
					Constants.API_VERS_NUM, returnUrlParams);
		} else {
			activationLink = String.format(activationEmailLink,
					activationServer, userAccount.getVerificationGuid(), currentApi,
					Constants.API_VERS_NUM);
		}

		String body = String.format(emailBody, userAccount.getUserName(),
				userAccount.getFullName(), activationLink, getImageTag());

		log.debug(String.format("Sending email to: %1$s",
				userAccount.getEmailAddress()));
		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Sends out a notification message for account deactivation
	 * 
	 * @param userAccount
	 */
	public void sendDeactivationMessage(UserAccount userAccount) {
		String emailBody = UserAccountEmailBody.deactivatedAccount;

		String subject = accountDeactivationSubject;

		String body = String.format(emailBody, supportEmailAddress, getImageTag());

		log.debug(String.format("Sending email to: %1$s",
				userAccount.getEmailAddress()));
		sendEmail(subject, body, userAccount.getEmailAddress());
	}


	public void sendDeactivationWarning(String email, int remainingDays) {
		String emailBody = UserAccountEmailBody.deactivationWarning;

		String subject = deactivationWarningSubject;

		String body = String.format(emailBody, supportEmailAddress, String.format("%1$d days", remainingDays), getImageTag());

		log.debug(String.format("Sending email to: %1$s", email));
		sendEmail(subject, body, email);
	}

	/**
	 * Sends out a notification message for account deactivation
	 * 
	 * @param userAccount
	 */
	public void sendDeactivationMessage(UserAccountValueObject userAccount) {
		String emailBody = UserAccountEmailBody.deactivatedAccount;

		String subject = accountDeactivationSubject;

		String body = String.format(emailBody, supportEmailAddress, getImageTag());

		log.debug(String.format("Sending email to: %1$s",
				userAccount.getEmailAddress()));
		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Sends out an email with the user name for the provided user account
	 * instance
	 * 
	 * @param userAccount
	 */
	public void sendUserNameRecovery(UserAccount userAccount) {
		String subject = userNameRecoveryEmailSubject;
		String body = String.format(UserAccountEmailBody.forgotUsernameBody,
				userAccount.getUserName(), supportEmailAddress, getImageTag());
		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Sends out an email with the user name for the provided user account
	 * instance
	 * 
	 * @param userAccount
	 */
	public void sendUserNameRecovery(UserAccountValueObject userAccount) {
		String subject = userNameRecoveryEmailSubject;
		String body = String.format(UserAccountEmailBody.forgotUsernameBody,
				userAccount.getUserName(), supportEmailAddress, getImageTag());
		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Sends out a message with the reset password link
	 * 
	 * @param userAccount
	 */
	public void sendPasswordReset(UserAccount userAccount) {
		String emailBody = UserAccountEmailBody.passwordChangeRequestBody;

		String subject = passwordResetEmailSubject;
		String verificationLink = String.format(passwordResetEmailLink,
				activationServer, userAccount.getVerificationGuid(), currentApi,
				Constants.API_VERS_NUM, userAccount.getUserName());

		String body = String.format(emailBody, verificationLink,
				supportEmailAddress, getImageTag());

		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Sends out a message with the reset password link
	 * 
	 * @param userAccount
	 */
	public void sendPasswordReset(UserAccountValueObject userAccount) {
		String emailBody = UserAccountEmailBody.passwordChangeRequestBody;

		String subject = passwordResetEmailSubject;
		String verificationLink = String.format(passwordResetEmailLink,
				activationServer, userAccount.getVerificationGuid(), currentApi,
				Constants.API_VERS_NUM, userAccount.getUserName());

		String body = String.format(emailBody, verificationLink,
				supportEmailAddress, getImageTag());

		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Sends out a message saying that the password reset failed
	 * 
	 * @param userAccount
	 */
	public void sendPasswordResetFailure(UserAccount userAccount) {
		String subject = passwordResetFailedEmailSubject;
		String body = passwordResetFailedEmailBody;
		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Sends out a message saying that the password reset failed
	 * 
	 * @param userAccount
	 */
	public void sendPasswordResetFailure(UserAccountValueObject userAccount) {
		String subject = passwordResetFailedEmailSubject;
		String body = passwordResetFailedEmailBody;
		sendEmail(subject, body, userAccount.getEmailAddress());
	}

	/**
	 * Implements a call to the email client
	 * 
	 * @param subject
	 *          The message subject
	 * @param body
	 *          The message body
	 * @param toAddress
	 *          A destination address where the message will be sent
	 */
	public void sendEmail(String subject, String body, String toAddress) {
		if (!sendEmailsToNaraTest
				&& toAddress.startsWith(NARA_TEST_EMAIL_BEGINS_WITH)) {
			return;
		}
		emailClient.getToAddresses().clear();
		emailClient.getToAddresses().add(toAddress);
		emailClient.SendMessage(subject, body);
	}

	/**
	 * Creates an image html tag
	 * 
	 * @return The tag text string
	 */
	private String getImageTag() {
		String resolvedImageTag = String.format(imageTag, activationServer
				+ emailNaraLogoPath);
		log.debug(resolvedImageTag);

		return resolvedImageTag;
	}

	public void sendEmailVerification(UserAccountValueObject userAccount) {
		String emailBody = UserAccountEmailBody.emailVerification;

		String subject = emailVerificationSubject;
		String activationLink = String.format(verifyEmailLink,
				activationServer, userAccount.getVerificationGuid(), currentApi,
				Constants.API_VERS_NUM);

		String body = String.format(emailBody, activationServer
				+ emailNaraLogoPath, userAccount.getUserName(), activationLink);

		log.debug(String.format("Sending email to: %1$s",
				userAccount.getAdditionalEmailAddress()));
		sendEmail(subject, body, userAccount.getAdditionalEmailAddress());
	}
}
