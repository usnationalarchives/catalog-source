package gov.nara.opa.api.system;

public class Config {

	private int searchRunTime;

	private int sessionTimeout;

	private int loginAttempts;
	private int timeLocked;
	private int attemptsTime;

	private int maxSearchResultsPublic;
	private int maxSearchResultsStandard;
	private int maxSearchResultsPower;

	private int maxApiSearchResults;

	private String invalidSearchStringPattern;

	private int maxResultsPerListStandard;
	private int maxResultsPerListPublic;
	private int maxResultsPerListPower;
	private int maxListsPerUser;

	private int maxPrintResults;
	
	private int maxRowsPerSearch;

	private int maxNonBulkTimer;
	private int maxNonBulkFileSizeLimit;
	private int maxBulkExportFileSize;
	private int bulkExpDays;

	private int maxNotificationRows;
	private int maxOpaTitlesRows;
	private int maxSummaryRows;
	private int maxContributionRows;

	private int transcriptionInactivityTime;

	private int transcriptionsDisplayTime;
	private int tagsDisplayTime;
	private int commentsDisplaytime;

	private int tagsLength;
	private int transcriptionsLength;
	private int commentsLength;
	private String commentsFormat;

	private String naraEmail;
	private String referringUrlPattern;

	private String bannerLinkHomeDisplay;
	private String bannerLinkHome;
	private String bannerLinkResearchRoomDisplay;
	private String bannerLinkResearchRoom;
	private String bannerLinkContactUsDisplay;
	private String bannerLinkContactUs;
	private String bannerLinkStatisticsDisplay;
	private String bannerLinkStatistics;
	private String bannerLinkHelpDisplay;
	private String bannerLinkHelp;

	private int maxTitlesPerRobotListing;

	public Config() {

	}

	public Config(int searchRunTime, int searchMaxResults, int tagsLength,
			int commentsLength, String commentsFormat) {
		this.searchRunTime = searchRunTime;
		this.tagsLength = tagsLength;
		this.commentsLength = commentsLength;
		this.commentsFormat = commentsFormat;
	}

	/**
	 * @return the searchRunTime
	 */
	public int getSearchRunTime() {
		return searchRunTime;
	}

	/**
	 * @param searchRunTime
	 *            the searchRunTime to set
	 */
	public void setSearchRunTime(int searchRunTime) {
		this.searchRunTime = searchRunTime;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	/**
	 * @return the attemptsTime
	 */
	public int getAttemptsTime() {
		return attemptsTime;
	}

	/**
	 * @param attemptsTime
	 *            the attemptsTime to set
	 */
	public void setAttemptsTime(int attemptsTime) {
		this.attemptsTime = attemptsTime;
	}

	/**
	 * @return the loginAttempts
	 */
	public int getLoginAttempts() {
		return loginAttempts;
	}

	/**
	 * @param loginAttempts
	 *            the loginAttempts to set
	 */
	public void setLoginAttempts(int loginAttempts) {
		this.loginAttempts = loginAttempts;
	}

	/**
	 * @return the timeLocked
	 */
	public int getTimeLocked() {
		return timeLocked;
	}

	/**
	 * @param timeLocked
	 *            the timeLocked to set
	 */
	public void setTimeLocked(int timeLocked) {
		this.timeLocked = timeLocked;
	}

	/**
	 * @return the maxSearchResultsPublic
	 */
	public int getMaxSearchResultsPublic() {
		return maxSearchResultsPublic;
	}

	/**
	 * @param maxSearchResultsPublic
	 *            the maxSearchResultsPublic to set
	 */
	public void setMaxSearchResultsPublic(int maxSearchResultsPublic) {
		this.maxSearchResultsPublic = maxSearchResultsPublic;
	}

	/**
	 * @return the maxSearchResultsStandard
	 */
	public int getMaxSearchResultsStandard() {
		return maxSearchResultsStandard;
	}

	/**
	 * @param maxSearchResultsStandard
	 *            the maxSearchResultsStandard to set
	 */
	public void setMaxSearchResultsStandard(int maxSearchResultsStandard) {
		this.maxSearchResultsStandard = maxSearchResultsStandard;
	}

	/**
	 * @return the maxSearchResultsPower
	 */
	public int getMaxSearchResultsPower() {
		return maxSearchResultsPower;
	}

	/**
	 * @param maxSearchResultsPower
	 *            the maxSearchResultsPower to set
	 */
	public void setMaxSearchResultsPower(int maxSearchResultsPower) {
		this.maxSearchResultsPower = maxSearchResultsPower;
	}

	/**
	 * @return the maxApiSearchResults
	 */
	public int getMaxApiSearchResults() {
		return maxApiSearchResults;
	}

	/**
	 * @param maxApiSearchResults
	 *            the maxApiSearchResults to set
	 */
	public void setMaxApiSearchResults(int maxApiSearchResults) {
		this.maxApiSearchResults = maxApiSearchResults;
	}

	/**
	 * @return the maxResultsPerListPublic
	 */
	public int getMaxResultsPerListPublic() {
		return maxResultsPerListPublic;
	}

	/**
	 * @param maxResultsPerListPublic
	 *            the maxResultsPerListPublic to set
	 */
	public void setMaxResultsPerListPublic(int maxResultsPerListPublic) {
		this.maxResultsPerListPublic = maxResultsPerListPublic;
	}

	/**
	 * @return the maxResultsPerListStandard
	 */
	public int getMaxResultsPerListStandard() {
		return maxResultsPerListStandard;
	}

	/**
	 * @param maxResultsPerListStandard
	 *            the maxResultsPerListStandard to set
	 */
	public void setMaxResultsPerListStandard(int maxResultsPerListStandard) {
		this.maxResultsPerListStandard = maxResultsPerListStandard;
	}

	/**
	 * @return the maxResultsPerListsPower
	 */
	public int getMaxResultsPerListPower() {
		return maxResultsPerListPower;
	}

	/**
	 * @param maxResultsPerListsPower
	 *            the maxResultsPerListsPower to set
	 */
	public void setMaxResultsPerListPower(int maxResultsPerListsPower) {
		this.maxResultsPerListPower = maxResultsPerListsPower;
	}

	/**
	 * @return the maxListsPerUser
	 */
	public int getMaxListsPerUser() {
		return maxListsPerUser;
	}

	/**
	 * @param maxListsPerUser
	 *            the maxListsPerUser to set
	 */
	public void setMaxListsPerUser(int maxListsPerUser) {
		this.maxListsPerUser = maxListsPerUser;
	}

	/**
	 * @return the maxNonBulkTimer
	 */
	public int getMaxNonBulkTimer() {
		return maxNonBulkTimer;
	}

	/**
	 * @param maxNonBulkTimer
	 *            the maxNonBulkTimer to set
	 */
	public void setMaxNonBulkTimer(int maxNonBulkTimer) {
		this.maxNonBulkTimer = maxNonBulkTimer;
	}

	/**
	 * @return the maxNonBulkFileSizeLimit
	 */
	public int getMaxNonBulkFileSizeLimit() {
		return maxNonBulkFileSizeLimit;
	}

	/**
	 * @param maxNonBulkFileSizeLimit
	 *            the maxNonBulkFileSizeLimit to set
	 */
	public void setMaxNonBulkFileSizeLimit(int maxNonBulkFileSizeLimit) {
		this.maxNonBulkFileSizeLimit = maxNonBulkFileSizeLimit;
	}

	/**
	 * @return the maxBulkExportFileSize
	 */
	public int getMaxBulkExportFileSize() {
		return maxBulkExportFileSize;
	}

	/**
	 * @param maxBulkExportFileSize
	 *            the maxBulkExportFileSize to set
	 */
	public void setMaxBulkExportFileSize(int maxBulkExportFileSize) {
		this.maxBulkExportFileSize = maxBulkExportFileSize;
	}

	/**
	 * @return the bulkExpDays
	 */
	public int getBulkExpDays() {
		return bulkExpDays;
	}

	/**
	 * @param bulkExpDays
	 *            the bulkExpDays to set
	 */
	public void setBulkExpDays(int bulkExpDays) {
		this.bulkExpDays = bulkExpDays;
	}

	public int getMaxPrintResults() {
		return maxPrintResults;
	}

	public void setMaxPrintResults(int maxPrintResults) {
		this.maxPrintResults = maxPrintResults;
	}

	public int getMaxNotificationRows() {
		return maxNotificationRows;
	}

	public void setMaxNotificationRows(int maxNotificationRows) {
		this.maxNotificationRows = maxNotificationRows;
	}

	public int getMaxOpaTitlesRows() {
		return maxOpaTitlesRows;
	}

	public void setMaxOpaTitlesRows(int maxOpaTitlesRows) {
		this.maxOpaTitlesRows = maxOpaTitlesRows;
	}

	public int getMaxSummaryRows() {
		return maxSummaryRows;
	}

	public void setMaxSummaryRows(int maxSummaryRows) {
		this.maxSummaryRows = maxSummaryRows;
	}

	public int getMaxContributionRows() {
		return maxContributionRows;
	}

	public void setMaxContributionRows(int maxContributionRows) {
		this.maxContributionRows = maxContributionRows;
	}

	/**
	 * @return the tagsLength
	 */
	public int getTagsLength() {
		return tagsLength;
	}

	/**
	 * @param tagsLength
	 *            the tagsLength to set
	 */
	public void setTagsLength(int tagsLength) {
		this.tagsLength = tagsLength;
	}

	public int getTranscriptionsLength() {
		return transcriptionsLength;
	}

	public void setTranscriptionsLength(int transcriptionsLength) {
		this.transcriptionsLength = transcriptionsLength;
	}

	/**
	 * @return the commentsFormat
	 */
	public String getCommentsFormat() {
		return commentsFormat;
	}

	/**
	 * @param commentsFormat
	 *            the commentsFormat to set
	 */
	public void setCommentsFormat(String commentsFormat) {
		this.commentsFormat = commentsFormat;
	}

	/**
	 * @return the commentsLength
	 */
	public int getCommentsLength() {
		return commentsLength;
	}

	/**
	 * @param commentsLength
	 *            the commentsLength to set
	 */
	public void setCommentsLength(int commentsLength) {
		this.commentsLength = commentsLength;
	}

	/**
	 * @return the transcriptionInactivityTime
	 */
	public int getTranscriptionInactivityTime() {
		return transcriptionInactivityTime;
	}

	/**
	 * @param transcriptionInactivityTime
	 *            the transcriptionInactivityTime to set
	 */
	public void setTranscriptionInactivityTime(int transcriptionInactivityTime) {
		this.transcriptionInactivityTime = transcriptionInactivityTime;
	}

	/**
	 * @return the transcriptionsDisplayTime
	 */
	public int getTranscriptionsDisplayTime() {
		return transcriptionsDisplayTime;
	}

	/**
	 * @param transcriptionsDisplayTime
	 *            the transcriptionsDisplayTime to set
	 */
	public void setTranscriptionsDisplayTime(int transcriptionsDisplayTime) {
		this.transcriptionsDisplayTime = transcriptionsDisplayTime;
	}

	/**
	 * @return the tagsDisplayTime
	 */
	public int getTagsDisplayTime() {
		return tagsDisplayTime;
	}

	/**
	 * @param tagsDisplayTime
	 *            the tagsDisplayTime to set
	 */
	public void setTagsDisplayTime(int tagsDisplayTime) {
		this.tagsDisplayTime = tagsDisplayTime;
	}

	/**
	 * @return the commentsDisplaytime
	 */
	public int getCommentsDisplayTime() {
		return commentsDisplaytime;
	}

	/**
	 * @param commentsDisplaytime
	 *            the commentsDisplaytime to set
	 */
	public void setCommentsDisplayTime(int commentsDisplaytime) {
		this.commentsDisplaytime = commentsDisplaytime;
	}

	/**
	 * @return the bannerLinkHomeDisplay
	 */
	public String getBannerLinkHomeDisplay() {
		return bannerLinkHomeDisplay;
	}

	/**
	 * @param bannerLinkHomeDisplay
	 *            the bannerLinkHomeDisplay to set
	 */
	public void setBannerLinkHomeDisplay(String bannerLinkHomeDisplay) {
		this.bannerLinkHomeDisplay = bannerLinkHomeDisplay;
	}

	/**
	 * @return the bannerLinkHome
	 */
	public String getBannerLinkHome() {
		return bannerLinkHome;
	}

	/**
	 * @param bannerLinkHome
	 *            the bannerLinkHome to set
	 */
	public void setBannerLinkHome(String bannerLinkHome) {
		this.bannerLinkHome = bannerLinkHome;
	}

	/**
	 * @return the bannerLinkResearchRoomDisplay
	 */
	public String getBannerLinkResearchRoomDisplay() {
		return bannerLinkResearchRoomDisplay;
	}

	/**
	 * @param bannerLinkResearchRoomDisplay
	 *            the bannerLinkResearchRoomDisplay to set
	 */
	public void setBannerLinkResearchRoomDisplay(
			String bannerLinkResearchRoomDisplay) {
		this.bannerLinkResearchRoomDisplay = bannerLinkResearchRoomDisplay;
	}

	/**
	 * @return the bannerLinkResearchRoom
	 */
	public String getBannerLinkResearchRoom() {
		return bannerLinkResearchRoom;
	}

	/**
	 * @param bannerLinkResearchRoom
	 *            the bannerLinkResearchRoom to set
	 */
	public void setBannerLinkResearchRoom(String bannerLinkResearchRoom) {
		this.bannerLinkResearchRoom = bannerLinkResearchRoom;
	}

	/**
	 * @return the bannerLinkContactUsDisplay
	 */
	public String getBannerLinkContactUsDisplay() {
		return bannerLinkContactUsDisplay;
	}

	/**
	 * @param bannerLinkContactUsDisplay
	 *            the bannerLinkContactUsDisplay to set
	 */
	public void setBannerLinkContactUsDisplay(String bannerLinkContactUsDisplay) {
		this.bannerLinkContactUsDisplay = bannerLinkContactUsDisplay;
	}

	/**
	 * @return the bannerLinkContactUs
	 */
	public String getBannerLinkContactUs() {
		return bannerLinkContactUs;
	}

	/**
	 * @param bannerLinkContactUs
	 *            the bannerLinkContactUs to set
	 */
	public void setBannerLinkContactUs(String bannerLinkContactUs) {
		this.bannerLinkContactUs = bannerLinkContactUs;
	}

	/**
	 * @return the bannerLinkStatisticsDisplay
	 */
	public String getBannerLinkStatisticsDisplay() {
		return bannerLinkStatisticsDisplay;
	}

	/**
	 * @param bannerLinkStatisticsDisplay
	 *            the bannerLinkStatisticsDisplay to set
	 */
	public void setBannerLinkStatisticsDisplay(
			String bannerLinkStatisticsDisplay) {
		this.bannerLinkStatisticsDisplay = bannerLinkStatisticsDisplay;
	}

	/**
	 * @return the bannerLinkStatistics
	 */
	public String getBannerLinkStatistics() {
		return bannerLinkStatistics;
	}

	/**
	 * @param bannerLinkStatistics
	 *            the bannerLinkStatistics to set
	 */
	public void setBannerLinkStatistics(String bannerLinkStatistics) {
		this.bannerLinkStatistics = bannerLinkStatistics;
	}

	/**
	 * @return the bannerLinkHelpDisplay
	 */
	public String getBannerLinkHelpDisplay() {
		return bannerLinkHelpDisplay;
	}

	/**
	 * @param bannerLinkHelpDisplay
	 *            the bannerLinkHelpDisplay to set
	 */
	public void setBannerLinkHelpDisplay(String bannerLinkHelpDisplay) {
		this.bannerLinkHelpDisplay = bannerLinkHelpDisplay;
	}

	/**
	 * @return the bannerLinkHelp
	 */
	public String getBannerLinkHelp() {
		return bannerLinkHelp;
	}

	/**
	 * @param bannerLinkHelp
	 *            the bannerLinkHelp to set
	 */
	public void setBannerLinkHelp(String bannerLinkHelp) {
		this.bannerLinkHelp = bannerLinkHelp;
	}

	public String getNaraEmail() {
		return naraEmail;
	}

	public void setNaraEmail(String naraEmail) {
		this.naraEmail = naraEmail;
	}

	public String getReferringUrlPattern() {
		return referringUrlPattern;
	}

	public void setReferringUrlPattern(String referringUrlPattern) {
		this.referringUrlPattern = referringUrlPattern;
	}

	public String getInvalidSearchStringPattern() {
		return invalidSearchStringPattern;
	}

	public void setInvalidSearchStringPattern(String invalidSearchStringPattern) {
		this.invalidSearchStringPattern = invalidSearchStringPattern;
	}

	/**
	 * @return the maxRowsPerSearch
	 */
	public int getMaxRowsPerSearch() {
		return maxRowsPerSearch;
	}

	/**
	 * @param maxRowsPerSearch the maxRowsPerSearch to set
	 */
	public void setMaxRowsPerSearch(int maxRowsPerSearch) {
		this.maxRowsPerSearch = maxRowsPerSearch;
	}

	/**
	 * @return the maxTitlesPerRobotListing
	 */
	public int getMaxTitlesPerRobotListing() {
		return maxTitlesPerRobotListing;
	}

	/**
	 * @param maxTitlesPerRobotListing the maxTitlesPerRobotListing to set
	 */
	public void setMaxTitlesPerRobotListing(int maxTitlesPerRobotListing) {
		this.maxTitlesPerRobotListing = maxTitlesPerRobotListing;
	}
}
