package gov.nara.opa.api.services.user.contributions;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.user.contributions.UserContributedTags;
import gov.nara.opa.api.validation.annotation.summary.SummaryRequestParameters;
import gov.nara.opa.api.valueobject.user.contributions.UserContributedCommentValueObject;
import gov.nara.opa.api.valueobject.user.contributions.UserContributionValueObject;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.util.List;

/**
 * Interface for the UserContributionsService that handles requests from the
 * controller
 */
public interface UserContributionsService {

	/**
	 * Select the rows from the annotation_tag table based on the parameters
	 * received
	 * 
	 * @param accountId
	 *            accountId used to filter the query
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @param tagText
	 *            tagText used to filter the query
	 * @param status
	 *            status used to filter the query
	 * @return Collection of the Tags found on the database
	 */
	public List<UserContributedTags> viewUserTags(String userName,
			String tagText, int status, int offset, int rows, String sort);

	/**
	 * Get totals tags for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return totals tags for the account logged
	 */
	public int getTotalTags(int accountId);

	/**
	 * Get totals tags for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return totals tags for the account logged
	 */
	public int getTotalTags(String userName);

	int getTotalComments(String userName);

	/**
	 * Get the detailed summary for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return Values required on the Summary
	 */
	public UserContributionValueObject getUserContributionsDetailSummary(
			int accountId);

	/**
	 * Get the brief summary for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return Values required on the Summary
	 */
	public UserContributionValueObject getUserContributionsBriefSummary(
			int accountId);

	/**
	 * Validate username exists
	 * 
	 * @param userName
	 *            Username to be valdiated
	 * @return true/false if username exists
	 */
	public boolean isValidUserName(String userName);

	/**
	 * Select the rows from the annotation_transcription table based on the
	 * parameters received
	 * 
	 * @param accountId
	 *            accountId used to filter the query
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @return Collection of the Transcription found on the database
	 */
	public List<Transcription> viewTranscriptions(int accountId, String naId,
			String objectId, int offset, int rows);

	/**
	 * Select the rows from the annotation_transcription table based on the
	 * parameters received
	 * 
	 * @param accountId
	 *            accountId used to filter the query
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @return Collection of the Transcription found on the database
	 */
	public List<Transcription> viewTranscriptions(String userName, String naId,
			String objectId, int offset, int rows);

	/**
	 * Select the rows from the annotation_tag table based on the parameters
	 * received
	 * 
	 * @param accountId
	 *            accountId used to filter the query
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @param tagText
	 *            tagText used to filter the query
	 * @param status
	 *            status used to filter the query
	 * @return Collection of the Tags found on the database
	 */
	public List<TagValueObject> viewTags(int accountId, String naId,
			String objectId, String tagText, int status, int offset, int rows);

	public List<TagValueObject> viewTags(
			SummaryRequestParameters requestParameters,
			ValidationResult validationResult, String tagText, int status);

	List<UserContributedCommentValueObject> selectUserComments(String userName,
			String title, int offset, int rows, boolean descOrder);
}
