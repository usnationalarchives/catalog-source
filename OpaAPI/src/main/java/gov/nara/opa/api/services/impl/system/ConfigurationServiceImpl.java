package gov.nara.opa.api.services.impl.system;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Config;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@Component
public class ConfigurationServiceImpl implements ConfigurationService {

	private static OpaLogger logger = OpaLogger
			.getLogger(ConfigurationServiceImpl.class);

	@Value("${configFilePath}")
	private String configFilePath;

	private static Config config = null;

	public Config getConfig() {
		return getConfig(configFilePath);
	}

	@Override
	public Config getConfig(String configXMLfilePath) {
		if (config == null) {

			config = new Config();

			try {
				File file = new File(configXMLfilePath);
				if (file.exists()) {
					DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder documentBuilder = documentBuilderFactory
							.newDocumentBuilder();
					Document document = documentBuilder.parse(file);

					// UI SETTINGS
					config.setSearchRunTime(Integer.parseInt(document
							.getElementsByTagName("searchRunTime").item(0)
							.getTextContent()));

					// SESSION SETTINGS
					config.setSessionTimeout((Integer.parseInt(document
							.getElementsByTagName("sessionTimeout").item(0)
							.getTextContent())));

					// FAILED LOGIN ATTEMPT SETTINGS
					config.setLoginAttempts((Integer.parseInt(document
							.getElementsByTagName("loginAttempts").item(0)
							.getTextContent())));
					config.setTimeLocked((Integer.parseInt(document
							.getElementsByTagName("timeLocked").item(0)
							.getTextContent())));
					config.setAttemptsTime((Integer.parseInt(document
							.getElementsByTagName("attemptsTime").item(0)
							.getTextContent())));

					// SEARCH SETTINGS
					config.setMaxSearchResultsStandard(Integer
							.parseInt(document
									.getElementsByTagName(
											"maxSearchResultsStandard").item(0)
									.getTextContent()));
					config.setMaxSearchResultsPublic(Integer.parseInt(document
							.getElementsByTagName("maxSearchResultsPublic")
							.item(0).getTextContent()));
					config.setMaxSearchResultsPower(Integer.parseInt(document
							.getElementsByTagName("maxSearchResultsPower")
							.item(0).getTextContent()));
					config.setInvalidSearchStringPattern(document
							.getElementsByTagName("invalidSearchStringPattern").item(0)
							.getTextContent());

					// PUBLIC API SEARCH SETTINGS
					config.setMaxApiSearchResults(Integer.parseInt(document
							.getElementsByTagName("maxApiSearchResults")
							.item(0).getTextContent()));

					// USER LIST SETTINGS
					config.setMaxResultsPerListStandard(Integer
							.parseInt(document
									.getElementsByTagName(
											"maxResultsPerListStandard")
									.item(0).getTextContent()));
					config.setMaxResultsPerListPublic(Integer.parseInt(document
							.getElementsByTagName("maxResultsPerListPublic")
							.item(0).getTextContent()));
					config.setMaxResultsPerListPower(Integer.parseInt(document
							.getElementsByTagName("maxResultsPerListPower")
							.item(0).getTextContent()));
					config.setMaxListsPerUser(Integer.parseInt(document
							.getElementsByTagName("maxListsPerUser").item(0)
							.getTextContent()));

					// PRINT SETTINGS
					config.setMaxPrintResults(Integer.parseInt(document
							.getElementsByTagName("maxPrintResults").item(0)
							.getTextContent()));

					// BULK EXPORT SETTINGS
					config.setMaxNonBulkTimer(Integer.parseInt(document
							.getElementsByTagName("maxNonBulkTimer").item(0)
							.getTextContent()));
					config.setMaxNonBulkFileSizeLimit(Integer.parseInt(document
							.getElementsByTagName("maxNonBulkFileSizeLimit")
							.item(0).getTextContent()));
					config.setMaxBulkExportFileSize(Integer.parseInt(document
							.getElementsByTagName("maxBulkExportFileSize")
							.item(0).getTextContent()));
					config.setBulkExpDays(Integer.parseInt(document
							.getElementsByTagName("bulkExpDays").item(0)
							.getTextContent()));

					// DB QUERY SETTINGS
					config.setMaxNotificationRows(Integer.parseInt(document
							.getElementsByTagName("maxNotificationRows")
							.item(0).getTextContent()));
					config.setMaxOpaTitlesRows(Integer.parseInt(document
							.getElementsByTagName("maxOpaTitlesRows").item(0)
							.getTextContent()));
					config.setMaxSummaryRows(Integer.parseInt(document
							.getElementsByTagName("maxSummaryRows").item(0)
							.getTextContent()));
					config.setMaxContributionRows(Integer.parseInt(document
							.getElementsByTagName("maxContributionRows")
							.item(0).getTextContent()));

					// ANNOTATION SETTINGS
					config.setTagsLength(Integer.parseInt(document
							.getElementsByTagName("tagsLength").item(0)
							.getTextContent()));
					config.setTranscriptionsLength(Integer.parseInt(document
							.getElementsByTagName("transcriptionsLength").item(0)
							.getTextContent()));					
					config.setCommentsLength(Integer.parseInt(document
							.getElementsByTagName("commentsLength").item(0)
							.getTextContent()));
					config.setCommentsFormat(document
							.getElementsByTagName("commentsFormat").item(0)
							.getTextContent());

					// TRANSCRIPTION INACTIVITY TIME SETTINGS
					config.setTranscriptionInactivityTime((Integer
							.parseInt(document
									.getElementsByTagName(
											"transcriptionInactivityTime")
									.item(0).getTextContent())));

					// CONTRIBUTION DISPLAY TIME SETTINGS
					config.setTranscriptionsDisplayTime((Integer
							.parseInt(document
									.getElementsByTagName(
											"transcriptionsDisplayTime")
									.item(0).getTextContent())));
					config.setTagsDisplayTime((Integer.parseInt(document
							.getElementsByTagName("tagsDisplayTime").item(0)
							.getTextContent())));
					config.setCommentsDisplayTime((Integer.parseInt(document
							.getElementsByTagName("commentsDisplayTime")
							.item(0).getTextContent())));

					// GENERAL SETTINGS
					config.setNaraEmail(document
							.getElementsByTagName("naraEmail").item(0)
							.getTextContent());

					config.setReferringUrlPattern(document
							.getElementsByTagName("referringUrlPattern").item(0)
							.getTextContent());

					// BANNER LINK SETTINGS
					config.setBannerLinkHomeDisplay(document
							.getElementsByTagName("bannerLinkHomeDisplay")
							.item(0).getTextContent());
					config.setBannerLinkHome(document
							.getElementsByTagName("bannerLinkHome").item(0)
							.getTextContent());
					config.setBannerLinkResearchRoomDisplay(document
							.getElementsByTagName(
									"bannerLinkResearchRoomDisplay").item(0)
							.getTextContent());
					config.setBannerLinkResearchRoom(document
							.getElementsByTagName("bannerLinkResearchRoom")
							.item(0).getTextContent());
					config.setBannerLinkContactUsDisplay(document
							.getElementsByTagName("bannerLinkContactUsDisplay")
							.item(0).getTextContent());
					config.setBannerLinkContactUs(document
							.getElementsByTagName("bannerLinkContactUs")
							.item(0).getTextContent());
					config.setBannerLinkStatisticsDisplay(document
							.getElementsByTagName("bannerLinkStatisticsDisplay")
							.item(0).getTextContent());
					config.setBannerLinkStatistics(document
							.getElementsByTagName("bannerLinkStatistics")
							.item(0).getTextContent());
					config.setBannerLinkHelpDisplay(document
							.getElementsByTagName("bannerLinkHelpDisplay")
							.item(0).getTextContent());
					config.setBannerLinkHelp(document
							.getElementsByTagName("bannerLinkHelp").item(0)
							.getTextContent());

					config.setMaxTitlesPerRobotListing(Integer.parseInt(document
							.getElementsByTagName("maxTitlesPerRobotListing").item(0)
							.getTextContent()));
				} else {
					return null;
				}

			} catch (SAXParseException err) {
				logger.error(err.getMessage(), err);
				System.out.println("** Parsing error" + ", line "
						+ err.getLineNumber() + ", uri " + err.getSystemId());
				System.out.println(" " + err.getMessage());

			} catch (SAXException e) {
				logger.error(e.getMessage(), e);

			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
			}
		}

		return config;
	}

	@Override
	public int getSearchLimitForUser(String accountType) {
		if (accountType != null && !accountType.equals("")) {
			switch (accountType) {
			case "standard":
				return getConfig().getMaxSearchResultsStandard();
			case "power":
				return getConfig().getMaxSearchResultsPower();
			}
		} else {
			return getConfig().getMaxSearchResultsPublic();
		}

		return 0;
	}

}
