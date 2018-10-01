package gov.nara.opa.api.dataaccess.impl.user.contributions;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.dataaccess.impl.annotation.transcriptions.TranscriptionExtractor;
import gov.nara.opa.api.dataaccess.user.contributions.UserContributionsDao;
import gov.nara.opa.api.user.contributions.UserContributedTags;
import gov.nara.opa.api.valueobject.user.contributions.UserContributedCommentValueObject;
import gov.nara.opa.api.valueobject.user.contributions.UserContributionValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.dataaccess.impl.annotation.tags.TagValueOjectResultSetExtractor;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class UserContributionsJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements UserContributionsDao {

	private static OpaLogger logger = OpaLogger
			.getLogger(UserContributionsJDBCTemplate.class);

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
	@Override
	public List<UserContributedTags> selectUserTags(String userName,
			String tagText, int status, int offset, int rows, String sort)
			throws DataAccessException, UnsupportedEncodingException {

		ArrayList<Object> paramsList = new ArrayList<Object>();

		// Retrieve the tag information
		String sql = "SELECT at.annotation, at.status, "
				+ " at.account_id, COUNT(1) as frequency "
				+ " FROM opadb.annotation_tags as at WHERE status = 1 ";

		if (userName != null && !userName.isEmpty()) {
			sql += " AND at.account_id = (select account_id from accounts where user_name = ? )  ";
			paramsList.add(userName);
		}
		if (tagText != null && !tagText.equals("")) {
			sql += " AND at.annotation = ? ";
			paramsList.add(tagText);
		}
		if (status != 0) {
			sql += " AND at.status = ? ";
			paramsList.add(status);
		} else {
			sql += " AND (at.status = 0 OR at.status = 1) ";
		}

		// Add the group by statement
		sql += " GROUP BY at.annotation, at.status,  at.account_id ";

		// Cofigure and add the order by statement
		sort = getSqlSort(sort);
		sql += String.format(" ORDER BY %1$s ", sort);
		// paramsList.add(sort);

		sql += " LIMIT ? OFFSET ?";

		// Add the LIMIT configuration
		paramsList.add(rows);
		paramsList.add(offset);

		logger.info(String.format("Sql statement: %1$s", sql));
		return getJdbcTemplate().query(
				sql,
				paramsList.toArray(),
				new GenericRowMapper<UserContributedTags>(
						new UserContributedTagsExtractor()));

	}

	/**
	 * Get the correct configuration supported by the sql query
	 * 
	 * @param sort
	 *            Sort value as received on the URL
	 * @return The supported value for the query
	 */
	private String getSqlSort(String sort) {
		if (sort != null && !sort.equals("")) {
			switch (sort) {

			case "tag DESC":
				sort = " annotation DESC ";
				break;
			case "tag ASC":
				sort = " annotation ASC ";
				break;
			case "count DESC":
				sort = " frequency DESC ";
				break;
			case "count ASC":
				sort = " frequency DESC ";
				break;
			default:
				sort = " annotation DESC ";
				break;
			}
		}
		return sort;
	}

	/**
	 * Get totals tags for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return totals tags for the account logged
	 */
	@SuppressWarnings("unchecked")
	public int getTotalTags(int accountId) {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);

		List<TagValueObject> tags = (List<TagValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetTagsByAccountId",
						new GenericRowMapper<TagValueObject>(
								new TagValueOjectResultSetExtractor()),
						inParamMap);

		if (tags != null && tags.size() > 0) {
			return tags.size();
		} else {
			return 0;
		}
	}

	/**
	 * Get totals tags for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return totals tags for the account logged
	 */
	@SuppressWarnings("unchecked")
	public int getTotalTags(String userName) {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("username", userName);

		List<TagValueObject> tags = (List<TagValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetTagsByUsername",
						new GenericRowMapper<TagValueObject>(
								new TagValueOjectResultSetExtractor()),
						inParamMap);

		if (tags != null && tags.size() > 0) {
			return tags.size();
		} else {
			return 0;
		}
	}

	/**
	 * Get the detailed summary for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return Values required on the Summary
	 */
	@SuppressWarnings("unchecked")
	@Override
	public UserContributionValueObject getUserContributionsDetailSummary(
			int accountId) throws DataAccessException,
			UnsupportedEncodingException {

		UserContributionValueObject result = new UserContributionValueObject();
		UserContributionValueObject contribution = null;

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);

		result = ((List<UserContributionValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(),
						"spGetTagCountForUserContributionSummary",
						new GenericRowMapper<UserContributionValueObject>(
								new UserContributionsExtractor()), inParamMap))
				.get(0);

		contribution = ((List<UserContributionValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(),
						"spGetCommentCountForUserContributionSummary",
						new GenericRowMapper<UserContributionValueObject>(
								new UserContributionsExtractor()), inParamMap))
				.get(0);

		result.setTotalComments(contribution.getTotalComments());
		result.setTotalCommentsMonth(contribution.getTotalCommentsMonth());
		result.setTotalCommentsYear(contribution.getTotalCommentsYear());

		contribution = ((List<UserContributionValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(),
						"spGetTranscriptionCountForUserContributionSummary",
						new GenericRowMapper<UserContributionValueObject>(
								new UserContributionsExtractor()), inParamMap))
				.get(0);

		result.setTotalTranscriptions(contribution.getTotalTranscriptions());
		result.setTotalTranscriptionsMonth(contribution
				.getTotalTranscriptionsMonth());
		result.setTotalTranscriptionsYear(contribution
				.getTotalTranscriptionsYear());

		result.setTotalContributions(result.getTotalTags()
				+ result.getTotalTranscriptions() + result.getTotalComments());
		result.setTotalContributionsMonth(result.getTotalTagsMonth()
				+ result.getTotalTranscriptionsMonth()
				+ result.getTotalCommentsMonth());
		result.setTotalContributionsYear(result.getTotalTagsYear()
				+ result.getTotalTranscriptionsYear()
				+ result.getTotalCommentsYear());

		return result;
	}

	/**
	 * Get the brief summary for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return Values required on the Summary
	 */
	@Override
	public UserContributionValueObject getUserContributionsBriefSummary(
			int accountId) throws DataAccessException,
			UnsupportedEncodingException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);

		int totalTags = StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(),
				"spGetTagCountForUserContributionBriefSummary", inParamMap,
				"count");

		int totalComments = getTotalComments(accountId);

		int totalTranscriptions = StoredProcedureDataAccessUtils
				.executeWithIntResult(
						getJdbcTemplate(),
						"spGetTranscriptionCountForUserContributionBriefSummary",
						inParamMap, "count");

		UserContributionValueObject result = new UserContributionValueObject();
		result.setTotalTags(totalTags);
		result.setTotalTranscriptions(totalTranscriptions);
		result.setTotalComments(totalComments);
		result.setTotalContributions(totalTranscriptions + totalTags
				+ totalComments);

		return result;
	}

	@Override
	public int getTotalComments(int accountId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);

		return StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(),
				"spGetCommentCountForUserContributionBriefSummary", inParamMap,
				"count");
	}

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
	@Override
	public List<Transcription> selectTranscriptions(int accountId, String naId,
			String objectId, int offset, int rows) throws DataAccessException,
			UnsupportedEncodingException {

		ArrayList<Object> paramsList = new ArrayList<Object>();

		String sql = "SELECT l.annotation_id, at.annotation, "
				+ " at.saved_vers_num, at.annotation_md5, "
				+ " l.first_annotation_id, l.status, l.na_id,  "
				+ " l.object_id, l.page_num, at.opa_id,  "
				+ " l.account_id, at.annotation_ts, a.user_name, "
				+ " a.full_name, a.is_nara_staff   "
				+ " FROM annotation_log l "
				+ " JOIN accounts a ON l.first_account_id = a.account_id "
				+ " JOIN annotation_transcriptions at ON l.first_annotation_id = at.annotation_id "
				+ " WHERE (l.status = 0 OR l.status = 1) "
				+ " AND (at.status = 0 OR at.status = 1) "
				+ " AND at.account_id = a.account_id "
				+ " AND at.first_annotation_id = at.annotation_id ";

		if (accountId != 0) {
			sql += " AND l.account_id = ? ";
			paramsList.add(accountId);
		}
		if (naId != null && !naId.equals("")) {
			sql += " AND l.na_id = ? ";
			paramsList.add(naId.getBytes("UTF-8"));
		}
		if (objectId != null && !objectId.equals("")) {
			sql += " AND l.object_id = ? ";
			paramsList.add(objectId.getBytes("UTF-8"));
		}
		sql += " GROUP BY l.na_id, l.object_id  ";
		// Add the LIMIT configuration
		sql += " LIMIT ?, ? ";
		paramsList.add(offset);
		paramsList.add(rows);

		return getJdbcTemplate().query(
				sql,
				paramsList.toArray(),
				new GenericRowMapper<Transcription>(
						new TranscriptionExtractor()));

	}

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
	@Override
	public List<Transcription> selectTranscriptions(String userName,
			String naId, String objectId, int offset, int rows)
			throws DataAccessException, UnsupportedEncodingException {

		ArrayList<Object> paramsList = new ArrayList<Object>();

		String sql = "SELECT l.annotation_id, at.annotation, "
				+ " at.saved_vers_num, at.annotation_md5, "
				+ " l.first_annotation_id, l.status, l.na_id,  "
				+ " l.object_id, l.page_num, at.opa_id,  "
				+ " l.account_id, at.annotation_ts, a.user_name, "
				+ " a.full_name, a.is_nara_staff   "
				+ " FROM annotation_log l "
				+ " JOIN accounts a ON l.first_account_id = a.account_id "
				+ " JOIN annotation_transcriptions at ON l.first_annotation_id = at.annotation_id "
				+ " WHERE at.account_id = a.account_id "
				+ " AND (l.status = 0 OR l.status = 1) "
				+ " AND (at.status = 0 OR at.status = 1) "
				+ " AND at.first_annotation_id = at.annotation_id ";

		if (userName != null && !userName.equals("")) {
			sql += " AND a.user_name = ? ";
			paramsList.add(userName);
		}
		if (naId != null && !naId.equals("")) {
			sql += " AND l.na_id = ? ";
			paramsList.add(naId.getBytes("UTF-8"));
		}
		if (objectId != null && !objectId.equals("")) {
			sql += " AND l.object_id = ? ";
			paramsList.add(objectId.getBytes("UTF-8"));
		}
		sql += " GROUP BY l.na_id, l.object_id  ";

		// Add the LIMIT configuration
		sql += " LIMIT ?, ? ";
		paramsList.add(offset);
		paramsList.add(rows);

		return getJdbcTemplate().query(
				sql,
				paramsList.toArray(),
				new GenericRowMapper<Transcription>(
						new TranscriptionExtractor()));

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TagValueObject> selectTagValueObjects(int accountId,
			String naId, String objectId, String tagText, int status,
			int offset, int rows) throws DataAccessException,
			UnsupportedEncodingException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("annotationText", tagText);
		inParamMap.put("annotationStatus", status);
		inParamMap.put("limOffset", offset);
		inParamMap.put("limRows", rows);

		return (List<TagValueObject>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spSummarySelectTags",
				new GenericRowMapper<TagValueObject>(
						new TagValueOjectResultSetExtractor()), inParamMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserContributedCommentValueObject> selectUserComments(
			int accountId, String title, int offset, int rows, boolean descOrder)
			throws DataAccessException, UnsupportedEncodingException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		inParamMap.put("title", title);
		inParamMap.put("limOffset", offset);
		inParamMap.put("limRows", rows);
		inParamMap.put("descOrder", descOrder);

		return (List<UserContributedCommentValueObject>) StoredProcedureDataAccessUtils
				.execute(
						getJdbcTemplate(),
						"spGetUserContributedCommentsByAccountId",
						new GenericRowMapper<UserContributedCommentValueObject>(
								new UserContributedCommentExtractor()),
						inParamMap);
	}
}
