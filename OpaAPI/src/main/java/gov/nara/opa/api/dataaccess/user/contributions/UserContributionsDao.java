package gov.nara.opa.api.dataaccess.user.contributions;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.user.contributions.UserContributedTags;
import gov.nara.opa.api.valueobject.user.contributions.UserContributedCommentValueObject;
import gov.nara.opa.api.valueobject.user.contributions.UserContributionValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;

public interface UserContributionsDao {

	/**
	 * Select the rows from the annotation_tag table based on the parameters
	 * received
	 * 
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
	List<UserContributedTags> selectUserTags(String userName, String tagText,
			int status, int offset, int rows, String sort)
			throws DataAccessException, UnsupportedEncodingException;

	/**
	 * Select the rows from the annotation_tag table based on the parameters
	 * received
	 * 
	 * @param naIds
	 *            naIds used to filter the query
	 * @return Collection of the Tags found on the database
	 */
	// public List<UserContributedTags> selectUserTags(String[] naIds, int
	// offset,
	// int rows) throws DataAccessException, UnsupportedEncodingException;

	/**
	 * Get the detailed summary for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return Values required on the Summary
	 */
	UserContributionValueObject getUserContributionsDetailSummary(int accountId)
			throws DataAccessException, UnsupportedEncodingException;

	/**
	 * Get the brief summary for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return Values required on the Summary
	 */
	UserContributionValueObject getUserContributionsBriefSummary(int accountId)
			throws DataAccessException, UnsupportedEncodingException;

	/**
	 * Get totals tags for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return totals tags for the account logged
	 */
	int getTotalTags(int accountId);

	/**
	 * Get totals tags for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return totals tags for the account logged
	 */
	int getTotalTags(String userName);

	/**
	 * Select the rows from the annotation_transcription table based on the
	 * parameters received
	 * 
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @return Collection of the Transcription found on the database
	 */
	List<Transcription> selectTranscriptions(String userName, String naId,
			String objectId, int offset, int rows) throws DataAccessException,
			UnsupportedEncodingException;

	/**
	 * Select the rows from the annotation_transcription table based on the
	 * parameters received
	 * 
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @return Collection of the Transcription found on the database
	 */
	List<Transcription> selectTranscriptions(int accountId, String naId,
			String objectId, int offset, int rows) throws DataAccessException,
			UnsupportedEncodingException;

	/**
	 * Select the rows from the annotation_tag table based on the parameters
	 * received
	 * 
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
	List<TagValueObject> selectTagValueObjects(int accountId, String naId,
			String objectId, String tagText, int status, int offset, int rows)
			throws DataAccessException, UnsupportedEncodingException;

	List<UserContributedCommentValueObject> selectUserComments(int accountId,
			String title, int offset, int rows, boolean descOrder)
			throws DataAccessException, UnsupportedEncodingException;

	int getTotalComments(int accountId);
}
