package gov.nara.opa.api.valueobject.user.contributions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class UserContributionValueObject extends AbstractWebEntityValueObject {

	private int totalTags;
	private int totalTagsMonth;
	private int totalTagsYear;
	private int totalTranscriptions;
	private int totalTranscriptionsMonth;
	private int totalTranscriptionsYear;
	private int totalComments;
	private int totalCommentsMonth;
	private int totalCommentsYear;
	private int totalContributions;
	private int totalContributionsMonth;
	private int totalContributionsYear;

	/**
	 * @return the totalContributionsYear
	 */
	public int getTotalContributionsYear() {
		return totalContributionsYear;
	}

	/**
	 * @param totalContributionsYear
	 *            the totalContributionsYear to set
	 */
	public void setTotalContributionsYear(int totalContributionsYear) {
		this.totalContributionsYear = totalContributionsYear;
	}

	/**
	 * @return the totalTags
	 */
	public int getTotalTags() {
		return totalTags;
	}

	/**
	 * @param totalTags
	 *            the totalTags to set
	 */
	public void setTotalTags(int totalTags) {
		this.totalTags = totalTags;
	}

	/**
	 * @return the totalTagsMonth
	 */
	public int getTotalTagsMonth() {
		return totalTagsMonth;
	}

	/**
	 * @param totalTagsMonth
	 *            the totalTagsMonth to set
	 */
	public void setTotalTagsMonth(int totalTagsMonth) {
		this.totalTagsMonth = totalTagsMonth;
	}

	/**
	 * @return the totalTagsYear
	 */
	public int getTotalTagsYear() {
		return totalTagsYear;
	}

	/**
	 * @param totalTagsYear
	 *            the totalTagsYear to set
	 */
	public void setTotalTagsYear(int totalTagsYear) {
		this.totalTagsYear = totalTagsYear;
	}

	/**
	 * @return the totalTranscriptions
	 */
	public int getTotalTranscriptions() {
		return totalTranscriptions;
	}

	/**
	 * @param totalTranscriptions
	 *            the totalTranscriptions to set
	 */
	public void setTotalTranscriptions(int totalTranscriptions) {
		this.totalTranscriptions = totalTranscriptions;
	}

	/**
	 * @return the totalTranscriptionsMonth
	 */
	public int getTotalTranscriptionsMonth() {
		return totalTranscriptionsMonth;
	}

	/**
	 * @param totalTranscriptionsMonth
	 *            the totalTranscriptionsMonth to set
	 */
	public void setTotalTranscriptionsMonth(int totalTranscriptionsMonth) {
		this.totalTranscriptionsMonth = totalTranscriptionsMonth;
	}

	/**
	 * @return the totalTranscriptionsYear
	 */
	public int getTotalTranscriptionsYear() {
		return totalTranscriptionsYear;
	}

	/**
	 * @param totalTranscriptionsYear
	 *            the totalTranscriptionsYear to set
	 */
	public void setTotalTranscriptionsYear(int totalTranscriptionsYear) {
		this.totalTranscriptionsYear = totalTranscriptionsYear;
	}

	/**
	 * @return the totalContributions
	 */
	public int getTotalContributions() {
		return totalContributions;
	}

	/**
	 * @param totalContributions
	 *            the totalContributions to set
	 */
	public void setTotalContributions(int totalContributions) {
		this.totalContributions = totalContributions;
	}

	/**
	 * @return the totalContributionsMonth
	 */
	public int getTotalContributionsMonth() {
		return totalContributionsMonth;
	}

	/**
	 * @param totalContributionsMonth
	 *            the totalContributionsMonth to set
	 */
	public void setTotalContributionsMonth(int totalContributionsMonth) {
		this.totalContributionsMonth = totalContributionsMonth;
	}

	/**
	 * @return the totalComments
	 */
	public int getTotalComments() {
		return totalComments;
	}

	/**
	 * @param totalComments
	 *            the totalComments to set
	 */
	public void setTotalComments(int totalComments) {
		this.totalComments = totalComments;
	}

	/**
	 * @return the totalCommentsMonth
	 */
	public int getTotalCommentsMonth() {
		return totalCommentsMonth;
	}

	/**
	 * @param totalCommentsMonth
	 *            the totalCommentsMonth to set
	 */
	public void setTotalCommentsMonth(int totalCommentsMonth) {
		this.totalCommentsMonth = totalCommentsMonth;
	}

	/**
	 * @return the totalCommentsYear
	 */
	public int getTotalCommentsYear() {
		return totalCommentsYear;
	}

	/**
	 * @param totalCommentsYear
	 *            the totalCommentsYear to set
	 */
	public void setTotalCommentsYear(int totalCommentsYear) {
		this.totalCommentsYear = totalCommentsYear;
	}
	
	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		return aspireContent;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new HashMap<String, Object>();
		return databaseContent;
	}
}
