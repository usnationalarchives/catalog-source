/**
 * Copyright Search Technologies 2013
 */
package gov.nara.opa.ingestion;

import java.io.IOException;
import java.text.ParseException;

import com.searchtechnologies.aspire.framework.utilities.StringUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;

/**
 * Downloads digital object file to opaIp/content/
 */
public class InitializeDasExportStage extends IngestionStage {
	Settings settings;

	@Override
	public void process(Job job) throws AspireException {
		settings = Components.getSettings(this);

		if (job != null) {

			String sourceUrl = job.get().get("connectorSource")
					.getText("sourceUrl");
			String folderPath = job.get().get("connectorSource")
					.getText("folderPath");
			String notifyEmails = job.get().get("connectorSource")
					.getText("notifyEmails");
			String daysToExpire = job.get().get("connectorSource")
					.getText("daysToExpire");

			try {
                // Start the process of downloading and deleting expired
                // files
                DasExportDownloader dasExport = new DasExportDownloader(this);
                String latestFileName = dasExport.startProcess(sourceUrl,
                        folderPath, Integer.parseInt(daysToExpire));

				// if there is at least one email address configured on the UI
                // if a new file was found and downloaded, report by email.
                if (StringUtilities.isNotEmpty(notifyEmails) &&
                        StringUtilities.isNotEmpty(latestFileName)) {
                    sendEmailNotifications(job, sourceUrl,
                            folderPath, notifyEmails);
				}
			} catch (Throwable ex) {
				throw new AspireException("download failed", ex, "Source URL: %s", sourceUrl);
			}
		}
	}

	public void sendEmailNotifications(Job job,
                                       String sourceUrl, String folderPath, String notifyEmails)
			throws AspireException, IOException, ParseException {

		DasEmailSender dasEmailSender = new DasEmailSender(this);
		String subject = "New DAS Export Downloaded";
		String body = String.format("DAS Export '%s' has been downloaded to '%s'", sourceUrl, folderPath);

		dasEmailSender.sendEmail(this, settings, job, subject, body, notifyEmails);
	}

}