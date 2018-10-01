/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import org.w3c.dom.Element;

import com.google.common.base.Strings;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;

/**
 * Stage to create thumbnail.
 * 
 * @author OPA Ingestion Team
 */
public class SendMessageStage extends IngestionStage {
	Settings settings;

	@Override
	public void initialize(Element config) throws AspireException {
		settings = Components.getSettings(this);
	}

	@Override
	public void process(Job job) throws AspireException {
		if (settings.sendMessageEnabled() && job.getSubJobErrorCount() == 0) {
			String host = settings.getMailSmtpHost();
			String port = settings.getMailSmtpPort();
			Boolean auth = settings.getMailSmtpAuth();
			Boolean tls = settings.getMailSmtpStartTLSEnable();
			String username = settings.getMailSmtpUsername();
			String pass = settings.getMailSmtpPassword();
			String fromAddress = settings.getMailSmtpFromAddress();
			String toAddresses = settings.getMailSmtpToAddresses();

			if (!Strings.isNullOrEmpty(host.trim())
					&& !Strings.isNullOrEmpty(port.trim()) && auth != null
					&& tls != null && !Strings.isNullOrEmpty(username.trim())
					&& !Strings.isNullOrEmpty(pass.trim())
					&& !Strings.isNullOrEmpty(fromAddress.trim())
					&& !Strings.isNullOrEmpty(toAddresses.trim())) {

				EmailClient emailClient = new EmailClient(this, host, port,
						auth, tls, username, pass, fromAddress, toAddresses);
				JobInfo jobInfo = Jobs.getJobInfo(job);
				Integer naId = jobInfo.getNAID();
				String subject = String.format(
						"Digital Objects Processed for naId %d", naId);
				String body = String
						.format("Digital objects have been updated for naId %d and are viewable in the sandbox",
								naId);
				// The following line is commented because is used only for sandbox testing
				// emailClient.SendMessage(subject, body);
			} else {
				debug("Some attributes needed for email in settings.xml file are not being configured");
			}
		}

		// This is the last step in record processing
		// the JobInfo associated with the current record
		// is closed.
		JobInfo jobInfo = Jobs.getJobInfo(job);
		if (jobInfo != null){
			OpaStorage opaStorage = jobInfo.getOpaStorage();
			if (opaStorage != null){
				opaStorage.close();
			}
		}
	}
}
