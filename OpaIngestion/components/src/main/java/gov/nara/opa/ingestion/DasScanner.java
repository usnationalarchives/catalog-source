/**
 * Copyright Search Technologies 2013
 */
package gov.nara.opa.ingestion;

import java.util.List;

import org.w3c.dom.Element;

import com.google.common.base.Strings;
import com.searchtechnologies.aspire.framework.utilities.Utilities;
import com.searchtechnologies.aspire.groupexpansion.mapdb.SpecialAclDB;
import com.searchtechnologies.aspire.groupexpansion.mapdb.UserGroupDB;
import com.searchtechnologies.aspire.groupexpansion.server.UserOrGroup;
import com.searchtechnologies.aspire.scanner.AbstractLinearScanner;
import com.searchtechnologies.aspire.scanner.ItemType;
import com.searchtechnologies.aspire.scanner.SourceInfo;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.logging.ALogger;

public class DasScanner extends AbstractLinearScanner {
	Settings settings;

	/**
	 * Does any additional component initialization required by the scanner.
	 * Occurs during component initialization.
	 * 
	 * @param config
	 *            Element object with the component configuration from the
	 *            application bundle.
	 */
	@Override
	public void doAdditionalInitialization(Element config)
			throws AspireException {
	}

    @Override
    public void onComplete(SourceInfo info) throws AspireException{
    }

    @Override
    public void onError(SourceInfo info) throws AspireException{
    }


    /**
	 * Initializes a new instance of DasInfo. Occurs every time a new source is
	 * created on the user interface right after the first scan is fired.
	 * 
	 * @param propertiesXml
	 *            AspireObject containing the "connectorSource" information that
	 *            is provided by the user interface.
	 * @return A new DasInfo object that contains all the necessary data to
	 *         start scanning.
	 * @throws AspireException
	 *             If there are issues parsing the given parameters.
	 */
	@Override
	public SourceInfo initializeSourceInfo(AspireObject propertiesXml)
			throws AspireException {

		DasSourceInfo info = new DasSourceInfo();
		info.useSnapshots(false);

		if (propertiesXml != null) {
            String url = propertiesXml.getText("url");
            if (url != null) {
                info.setInputDir(url);
                info.initialize();
            }
		}

		return info;
	}

	/**
	 * Creates a new instance of the ItemType specific to the Scanner.
	 * 
	 * @return a new ItemType instance.
	 */
	@Override
	public ItemType newItemType() {
		return new DasItemType();
	}

	@Override
	public void performScan(SourceInfo info) throws AspireException {

		settings = Components.getSettings(this);
		String host = settings.getMailSmtpHost();
		String port = settings.getMailSmtpPort();
		Boolean auth = settings.getMailSmtpAuth();
		String pass = settings.getMailSmtpPassword();
		String username = settings.getMailSmtpUsername();
		Boolean tls = settings.getMailSmtpStartTLSEnable();
		String fromAddress = settings.getMailSmtpFromAddress();
		String toAddresses = info.getPropertiesXml().getText("notifyEmails");

        sendStartEmailNotifications(this, host, port, auth, tls, username,
                pass, fromAddress, toAddresses);

        super.performScan(info);

        sendFinishEmailNotifications(this, host, port, auth, tls, username,
                pass, fromAddress, toAddresses);
    }

	public void sendStartEmailNotifications(Component component, String host,
			String port, Boolean auth, Boolean tls, String username,
			String pass, String fromAddress, String toAddresses) {

		if (!Strings.isNullOrEmpty(toAddresses)) {

			if (!Strings.isNullOrEmpty(host.trim())
					&& !Strings.isNullOrEmpty(port.trim()) && auth != null
					&& tls != null && !Strings.isNullOrEmpty(username.trim())
					&& !Strings.isNullOrEmpty(pass.trim())
					&& !Strings.isNullOrEmpty(fromAddress.trim())
					&& !Strings.isNullOrEmpty(toAddresses.trim())) {

				EmailClient emailClient = new EmailClient(component, host,
						port, auth, tls, username, pass, fromAddress,
						toAddresses);
				String subject = String
						.format("The update process from DAS has started");
				String body = String
						.format("The latest DAS XML updates are being sent at this moment to the ingestion pipeline. "
								+ "You will receive a new notification email when the process is done.");

                try {
                    emailClient.SendMessage(subject, body);
                } catch (AspireException e) {
                    error(e, "failed to send email");
                }

            } else {
				debug("Some attributes needed for email in settings.xml file are not being configured");
			}
		}
	}

	public void sendFinishEmailNotifications(Component component, String host,
			String port, Boolean auth, Boolean tls, String username,
			String pass, String fromAddress, String toAddresses) {

		if (!Strings.isNullOrEmpty(toAddresses)) {

			if (!Strings.isNullOrEmpty(host.trim())
					&& !Strings.isNullOrEmpty(port.trim()) && auth != null
					&& tls != null && !Strings.isNullOrEmpty(username.trim())
					&& !Strings.isNullOrEmpty(pass.trim())
					&& !Strings.isNullOrEmpty(fromAddress.trim())
					&& !Strings.isNullOrEmpty(toAddresses.trim())) {

				EmailClient emailClient = new EmailClient(component, host,
						port, auth, tls, username, pass, fromAddress,
						toAddresses);
				String subject = String
						.format("The update process from DAS has completed successfully.");
				String body = String
						.format("The latest DAS updates have been submitted successfully to the ingestion pipeline.");
                try {
                    emailClient.SendMessage(subject, body);
                } catch (AspireException e) {
                    error(e, "failed to send email");
                }

            } else {
				debug("Some attributes needed for email in settings.xml file are not being configured");
			}
		}
	}

	/**
	 * If caching groups and users for expansion, this method is called
	 * periodically to download all users and groups for expansion
	 */
	@Override
	public boolean downloadUsersAndGroups(ALogger logger, SourceInfo si,
			UserGroupDB userGroupMap, List<UserOrGroup> externalUserGroupList)
			throws AspireException {
		return true;
	}

	/**
	 * Download a list of any special acls and return them to the scanner for
	 * use later
	 */
	@Override
	public boolean downloadSpecialAcls(ALogger logger, SourceInfo si,
			SpecialAclDB specialAcls) throws AspireException {
		return true;
	}

	/**
	 * This method will be called for each user and for each special acl that
	 * has been downloaded. The user and all its groups are passed along with
	 * the special acl. Decide if the user has access and return true if so and
	 * false otherwise
	 */
	@Override
	public boolean canAccessSpecialAcl(String specialAcl, UserOrGroup uog,
			List<UserOrGroup> grps) {
		return true;
	}
}
