/**
 * Copyright Search Technologies 2013
 */
package gov.nara.opa.ingestion;

import java.io.IOException;
import java.text.ParseException;

import com.google.common.base.Strings;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;

/**
 * Downloads the tar.gz file that PPC provides
 */
public class DasEmailSender {

	private final ALogger logger;

	public DasEmailSender(Component component) throws AspireException,
			IOException, ParseException {
		this.logger = (ALogger) component;

	}

	/**
	 * Configure and send the email
	 * 
	 * @param component
	 *            This interface describes the methods which are supported by
	 *            all Aspire components.
	 * @param settings
	 *            Contains the values of properties defined in file settings.xml
	 * @param job
	 *            Interface for Job
	 * @param subject
	 *            The custom Subject that the email will contain
	 * @param body
	 *            The body of the email.
	 * @throws IOException
	 * @throws ParseException
	 * @throws AspireException
	 */
	public void sendEmail(Component component, Settings settings, Job job,
			String subject, String body, String notifyEmails) throws IOException, ParseException,
			AspireException {

		if (settings.sendMessageEnabled() && job.getSubJobErrorCount() == 0) {
			String host = settings.getMailSmtpHost();
			String port = settings.getMailSmtpPort();
			Boolean auth = settings.getMailSmtpAuth();
			Boolean tls = settings.getMailSmtpStartTLSEnable();
			String username = settings.getMailSmtpUsername();
			String pass = settings.getMailSmtpPassword();
			String fromAddress = settings.getMailSmtpFromAddress();
			String toAddresses = notifyEmails;

			if (!Strings.isNullOrEmpty(host.trim())
					&& !Strings.isNullOrEmpty(port.trim()) && auth != null
					&& tls != null && !Strings.isNullOrEmpty(username.trim())
					&& !Strings.isNullOrEmpty(pass.trim())
					&& !Strings.isNullOrEmpty(fromAddress.trim())
					&& !Strings.isNullOrEmpty(toAddresses.trim())) {

				EmailClient emailClient = new EmailClient(component, host,
						port, auth, tls, username, pass, fromAddress,
						toAddresses);

				emailClient.SendMessage(subject, body);
			} else {
				logger.debug("Some attributes needed for email in settings.xml file are not being configured");
			}
		}
	}

}
