package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.dataaccess.moderator.ModeratorStreamDao;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ModeratorStreamJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements ModeratorStreamDao {

	private static final String STATUS_FILTER = "(annotation_log.status = 0 OR annotation_log.status = 1) ";

	// BEGIN TAG STREAM
	private static final String TAG_STREAM_COLUMNS = "SELECT annotation_log.log_id, annotation_log.annotation_type, "
			+ "annotation_log.annotation_id, "
			+ "'on', annotation_log.log_ts,   accounts.user_name, accounts.full_name, accounts.display_name_flag, accounts.is_nara_staff, "
			+ "annotation_log.action,  annotation_log.notes, CONVERT(opa_titles.opa_title USING 'UTF8') opa_title, opa_titles.opa_type, "
			+ "opa_titles.total_pages, "
			+ "annotation_tags.annotation as tag_text, annotation_log.na_id, annotation_reasons.reason,  "
			+ "annotation_tags.object_id, annotation_tags.annotation_ts as TG_ts, "
			+ "authors.user_name author_user_name, authors.full_name author_full_name, authors.display_name_flag author_display_name_flag, "
			+ "authors.is_nara_staff author_is_nara_staff, "
			+ "annotation_tags.page_num ";

	private static final String SELECT_DELETED_ANNOTATION_IDS = "SELECT annotation_id "
			+ "FROM annotation_log "
			+ "WHERE action = 'DELETE' "
			+ "AND (status = 0 OR status = 1) ";

	private static final String SELECT_DISTINCT_TAGS_IN_STREAM = "SELECT distinct annotation_log.annotation_id, annotation_log.annotation_type "
			+ "    FROM annotation_log "
			+ "    JOIN annotation_tags ON annotation_log.annotation_id = annotation_tags.annotation_id "
			+ "    LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id ";

	private static final String TAGS_IN_STREAM_WHERE = "WHERE annotation_type = 'TG' "
			+ " AND (annotation_log.status = 0 or annotation_log.status = 1) "
			+ " AND annotation_log.log_ts > date_sub(now(), INTERVAL ? DAY) "
			+ " AND annotation_log.annotation_id not in "
			+ " ("
			+ SELECT_DELETED_ANNOTATION_IDS
			+ ") %1$s "
			+ " ORDER BY annotation_tags.annotation_ts DESC ";

	private static final String TAGS_IN_STREAM_LIMIT = " LIMIT ?,?";

	private static final String TAGS_NAID_FILTER = " (annotation_log.na_id = ? "
			+ "    OR convert(opa_titles.opa_title using UTF8) like ? "
			+ "    OR annotation_tags.annotation like ?) ";

	private static final String TAG_STREAM_JOINS = "JOIN annotation_log ON annotation_log.annotation_id = tag_ids.annotation_id "
			+ "AND annotation_log.annotation_type = tag_ids.annotation_type "
			+ "JOIN accounts ON annotation_log.account_id = accounts.account_id "
			+ "JOIN annotation_tags ON annotation_log.annotation_id = annotation_tags.annotation_id "
			+ "JOIN accounts authors ON annotation_tags.account_id = authors.account_id "
			+ "LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id "
			+ "LEFT JOIN annotation_reasons ON annotation_log.reason_id = annotation_reasons.reason_id "
			+ "AND annotation_reasons.reason_status = 1 ";
	// END TAG STREAM

	// BEGIN TRANSCRIPTION STREAM
	private static final String TRANSCRIPTION_STREAM_SELECT = "SELECT annotation_log.log_id, annotation_log.annotation_type, annotation_log.annotation_id, "
			+ "annotation_log.log_ts, accounts.user_name, accounts.full_name, accounts.display_name_flag, accounts.is_nara_staff, "
			+ "annotation_log.action,   annotation_log.notes, CONVERT(opa_titles.opa_title USING 'UTF8') opa_title, opa_titles.opa_type, "
			+ "opa_titles.total_pages, "
			+ "convert(t.annotation using UTF8) as transcription_teaser,"
			+ "annotation_log.na_id ,  annotation_log.object_id, annotation_reasons.reason, "
			+ "annotation_log.notes, annotation_log.version_num, t.annotation_ts as TR_ts, "
			+ "authors.user_name author_user_name, authors.full_name author_full_name, authors.display_name_flag author_display_name_flag, "
			+ "authors.is_nara_staff author_is_nara_staff, " + "t.page_num ";

	private static final String TRANSCRIPTION_NAID_FILTER = "(annotation_log.na_id = ? "
			+ "OR convert(tr.annotation using UTF8) like ? "
			+ "OR convert(opa_titles.opa_title using UTF8) like ? ) ";

	private static final String TRANSCRIPTION_STREAM_GROUPED_TRANSCRIPTIONS = "SELECT annotation_log.na_id, annotation_log.object_id, MAX(tr.annotation_id) id "
			+ "      FROM annotation_log "
			+ "      JOIN annotation_transcriptions tr ON annotation_log.annotation_id = tr.annotation_id "
			+ "      LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id "
			+ "      WHERE annotation_type = 'TR' "
			+ "      AND (annotation_log.status = 0 OR annotation_log.status = 1) "
			+ "      AND annotation_log.first_annotation_id NOT IN "
			+ "           (%1$s) %2$s "
			+ "      GROUP BY annotation_log.na_id, annotation_log.object_id ORDER BY id DESC LIMIT ?,?";

	private static final String TRANSCRIPTION_STREAM_JOINS = "JOIN annotation_transcriptions t ON grouped_t.na_id = t.na_id AND grouped_t.object_id = t.object_id "
			+ "JOIN annotation_log ON annotation_log.annotation_id = t.annotation_id "
			+ "AND  annotation_log.annotation_type = 'TR'   "
			+ "JOIN accounts ON annotation_log.account_id = accounts.account_id "
			+ "JOIN accounts authors ON t.account_id = authors.account_id "
			+ "LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id   "
			+ "LEFT JOIN annotation_reasons ON annotation_log.reason_id = annotation_reasons.reason_id "
			+ "AND annotation_reasons.reason_status = 1 ";

	private static final String TRANSCRIPTION_DELETED_FIRST_ANNOTATION_IDS = "SELECT DISTINCT first_annotation_id "
			+ "FROM annotation_log "
			+ "WHERE (status = 0 OR status = 1) "
			+ "AND annotation_type = 'TR' AND action = 'DELETE' ";

	// END TRANSCRIPTION STREAM

	// BEGIN MODERATOR STREAM
	private static final String MODERATOR_STREAM_SELECT = "SELECT annotation_log.log_id, annotation_log.annotation_type, annotation_log.annotation_id, 'on', "
			+ "annotation_log.log_ts, accounts.user_name, accounts.full_name, accounts.display_name_flag, accounts.is_nara_staff, "
			+ "annotation_log.action, annotation_log.notes, CONVERT(opa_titles.opa_title USING 'UTF8') opa_title, opa_titles.opa_type, "
			+ "opa_titles.total_pages, annotation_tags.annotation as tag_text, annotation_comments.annotation as comment_text, "
			+ "convert(annotation_transcriptions.annotation using UTF8) as transcription_teaser, annotation_log.na_id, "
			+ "annotation_log.object_id, annotation_reasons.reason, annotation_log.notes, annotation_log.version_num,"
			+ "annotation_transcriptions.annotation_ts as TR_ts, annotation_tags.annotation_ts as TG_ts, annotation_comments.annotation_ts as CM_ts, "
			+ "COALESCE(authors_tr.user_name, authors_tg.user_name, authors_cm.user_name) author_user_name, "
			+ "COALESCE(authors_tr.full_name, authors_tg.full_name, authors_cm.full_name) author_full_name, "
			+ "COALESCE(authors_tr.display_name_flag, authors_tg.display_name_flag, authors_cm.display_name_flag) author_display_name_flag, "
			+ "COALESCE(authors_tr.is_nara_staff, authors_tg.is_nara_staff, authors_cm.is_nara_staff) author_is_nara_staff, "
			+ "COALESCE(annotation_transcriptions.page_num, annotation_tags.page_num, annotation_comments.page_num) page_num "
			+ "JOIN accounts on annotation_log.account_id = accounts.account_id "
			+ "LEFT JOIN annotation_transcriptions ON annotation_log.annotation_id = annotation_transcriptions.annotation_id "
			+ "AND annotation_log.annotation_type = 'TR' "
			+ "AND annotation_log.log_ts > date_sub(now(), INTERVAL ? DAY) "
			+ "LEFT JOIN annotation_comments ON annotation_log.annotation_id = annotation_comments.annotation_id "
			+ "AND annotation_log.annotation_type = 'CM' "
			+ "AND annotation_log.log_ts > date_sub(now(), INTERVAL ? DAY) "
			+ "LEFT JOIN annotation_tags ON annotation_log.annotation_id = annotation_tags.annotation_id "
			+ "AND annotation_log.annotation_type = 'TG' "
			+ "AND annotation_log.log_ts > date_sub(now(), INTERVAL 180 DAY) "
			+ "LEFT JOIN annotation_log alTags ON annotation_tags.annotation_id = alTags.annotation_id "
			+ "AND alTags.annotation_type = 'TG' "
			+ "AND alTags.action = 'DELETE' AND alTags.status IN (0, 1) "
			+ "LEFT JOIN annotation_log alTrans ON annotation_log.first_annotation_id = alTrans.first_annotation_id "
			+ "AND alTrans.annotation_type = 'TR' "
			+ "AND alTrans.action = 'DELETE' AND alTrans.status IN (0, 1) "
			+ "LEFT JOIN annotation_log alComs ON annotation_log.first_annotation_id = alTrans.first_annotation_id "
			+ "AND alComs.annotation_type = 'CM' "
			+ "AND alComs.action = 'DELETE' AND alComs.status IN (0, 1) "
			+ "LEFT JOIN accounts authors_tr ON annotation_transcriptions.account_id = authors_tr.account_id "
			+ "LEFT JOIN accounts authors_cm ON annotation_comments.account_id = authors_cm.account_id "
			+ "LEFT JOIN accounts authors_tg ON annotation_tags.account_id = authors_tg.account_id "
			+ "LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id "
			+ "LEFT JOIN annotation_reasons ON annotation_log.reason_id = annotation_reasons.reason_id "
			+ "AND annotation_reasons.reason_status = 1 "
			+ "WHERE annotation_log.action IN (''REMOVE'', ''RESTORE'') "
			+ "AND alTrans.annotation_id IS NULL "
			+ "AND alComs.annotation_id IS NULL "
			+ "AND alTags.annotation_id IS NULL "
			+ "AND (annotation_log.status = 0 OR annotation_log.status = 1) "
			+ "AND (? IS NULL OR (annotation_log.na_id = ?  "
			+ "OR convert(opa_titles.opa_title using UTF8) like ? "
			+ "OR annotation_tags.annotation like ?  "
			+ "OR convert(annotation_transcriptions.annotation using UTF8) like ? ) "
			+ "OR convert(annotation_comments.annotation using UTF8) like ? )) "
			+ "ORDER BY log_ts DESC, log_id DESC " + "LIMIT ?,?";

	private static final String MODERATOR_STREAM_TAG_COUNT_NAID_FILTER = "(annotation_log.na_id = ? "
			+ "OR at.annotation like ? "
			+ "OR convert(opa_titles.opa_title using UTF8) like ? )";

	// END MODERATOR STREAM

	// BEGIN COMMENT STREAM
	private static final String COMMENT_NAID_FILTER = "(annotation_log.na_id = ? "
			+ "OR convert(cm.annotation using UTF8) like ? "
			+ "OR convert(opa_titles.opa_title using UTF8) like ? ) ";

	// END COMMENT STREAM

	@Override
	public List<Map<String, Object>> getTagStream(int offset, int rows,
			String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		return getTagStream(offset, rows, naId, displayTime, false);
	}

	@Override
	public List<Map<String, Object>> getTagStream(int offset, int rows,
			String naId, int displayTime, boolean useSp)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {

		if (!useSp) {
			return getTagStreamLegacy(offset, rows, naId, displayTime);
		} else {
			return getTagStreamSp(offset, rows, naId, displayTime);
		}
	}

	private List<Map<String, Object>> getTagStreamLegacy(int offset, int rows,
			String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {

		String select = TAG_STREAM_COLUMNS
				+ "FROM "
				+ "( "
				+ SELECT_DISTINCT_TAGS_IN_STREAM
				+ String.format(TAGS_IN_STREAM_WHERE,
						(naId != null && !naId.isEmpty() ? "AND "
								+ TAGS_NAID_FILTER : ""))
				+ TAGS_IN_STREAM_LIMIT
				+ ") tag_ids "
				+ TAG_STREAM_JOINS
				+ (naId != null && !naId.isEmpty() ? "    WHERE "
						+ TAGS_NAID_FILTER : "")
				+ "ORDER BY annotation_log.log_ts DESC, annotation_log.log_id DESC ";

		Object[] paramArray;
		if (naId != null && !naId.isEmpty()) {
			paramArray = new Object[] { displayTime, naId, "%" + naId + "%",
					"%" + naId + "%", offset, rows, naId, "%" + naId + "%",
					"%" + naId + "%" };
		} else {
			paramArray = new Object[] { displayTime, offset, rows };
		}

		List<Map<String, Object>> result = getJdbcTemplate().query(
				select,
				paramArray,
				new GenericRowMapper<Map<String, Object>>(
						new ModeratorStreamExtractor()));

		return getFilteredTagResults(result);

	}

	private List<Map<String, Object>> getTagStreamSp(int offset, int rows,
			String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("streamOffset", offset);
		inParamMap.put("streamRows", rows);
		inParamMap.put("naId", naId);
		inParamMap.put("displayTime", displayTime);

		List<Map<String, Object>> result = StoredProcedureDataAccessUtils
				.executeGeneric(getJdbcTemplate(), "spGetTagStream",
						new GenericRowMapper<Map<String, Object>>(
								new ModeratorStreamExtractor()), inParamMap);

		return getFilteredTagResults(result);
	}

	private List<Map<String, Object>> getFilteredTagResults(
			List<Map<String, Object>> result) {

		// Filter deleted results
		List<Map<String, Object>> filteredResult = new LinkedList<Map<String, Object>>();
		HashSet<String> deleted = new HashSet<String>();

		for (Map<String, Object> tagMap : result) {

			String key = getKey(tagMap);

			if (tagMap.get("action").toString().equals("DELETE")) {
				// If it was deleted, add to deleted HashSet
				deleted.add(key);
			} else if (!deleted.contains(key)) {
				// Add to filtered results if not deleted and not previous to a
				// delete
				filteredResult.add(tagMap);
			}
		}

		return filteredResult;
	}

	private String getKey(Map<String, Object> mapItem) {

		String key = mapItem.get("naId").toString()
				+ (mapItem.get("objectId") != null ? mapItem.get("objectId")
						.toString() : " ").toString()
				+ mapItem.get("text").toString();

		return key;
	}

	@Override
	public int getTagTotals(int displayTime) {
		return getTagTotals(null, displayTime);
	}

	@Override
	public int getTagTotals(String naId, int displayTime) {
		return getTagTotals(null, displayTime, false);
	}

	@Override
	public int getTagTotals(String naId, int displayTime, boolean useSp) {
		if (!useSp) {
			return getTagTotalsLegacy(naId, displayTime);
		} else {
			return getTagTotalsSp(naId, displayTime);
		}
	}

	private int getTagTotalsLegacy(String naId, int displayTime) {

		String select = "SELECT count(1) AS tag_count "
				+ "FROM (SELECT opa_titles.opa_title, "
				+ "annotation_tags.annotation as tag_text, annotation_log.na_id, annotation_log.object_id, annotation_tags.account_id "
				+ "FROM "
				+ "("
				+ SELECT_DISTINCT_TAGS_IN_STREAM
				+ String.format(TAGS_IN_STREAM_WHERE,
						(naId != null && !naId.isEmpty() ? "AND "
								+ TAGS_NAID_FILTER : ""))
				+ ") tag_ids "
				+ TAG_STREAM_JOINS
				+ (naId != null && !naId.isEmpty() ? "    WHERE "
						+ TAGS_NAID_FILTER : "")
				+ "GROUP BY opa_titles.opa_title, "
				+ "annotation_tags.annotation, annotation_log.na_id, annotation_log.object_id, annotation_tags.account_id ) tag_group";

		Object[] paramArray;

		if (naId != null && !naId.isEmpty()) {
			paramArray = new Object[] { displayTime, naId, "%" + naId + "%",
					"%" + naId + "%", naId, "%" + naId + "%", "%" + naId + "%" };
		} else {
			paramArray = new Object[] { displayTime };
		}

		int result = getJdbcTemplate().queryForInt(select, paramArray);
		return result;
	}

	private int getTagTotalsSp(String naId, int displayTime) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("displayTime", displayTime);

		int result = StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(), "spGetTagCount", inParamMap, "tag_count");

		return result;
	}

	public List<Map<String, Object>> getTranscriptionStream(int offset,
			int rows, String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		return getTranscriptionStream(offset, rows, naId, displayTime, false);
	}

	@Override
	public List<Map<String, Object>> getTranscriptionStream(int offset,
			int rows, String naId, int displayTime, boolean useSp)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		if (!useSp) {
			return getTranscriptionStreamLegacy(offset, rows, naId, displayTime);
		} else {
			return getTranscriptionStreamSp(offset, rows, naId, displayTime);
		}
	}

	private List<Map<String, Object>> getTranscriptionStreamLegacy(int offset,
			int rows, String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {

		String groupedTranscriptions = String.format(
				TRANSCRIPTION_STREAM_GROUPED_TRANSCRIPTIONS,
				TRANSCRIPTION_DELETED_FIRST_ANNOTATION_IDS,
				(!StringUtils.isNullOrEmtpy(naId) ? "      AND "
						+ TRANSCRIPTION_NAID_FILTER : ""));

		String select = TRANSCRIPTION_STREAM_SELECT
				+ "FROM ("
				+ groupedTranscriptions
				+ ") grouped_t "
				+ TRANSCRIPTION_STREAM_JOINS
				+ "WHERE annotation_log.log_ts > date_sub(now(), INTERVAL ? DAY) "
				+ "ORDER BY annotation_log.log_ts DESC, annotation_log.log_id DESC  ";

		Object[] paramArray;

		if (naId != null && !naId.isEmpty()) {
			paramArray = new Object[] { naId, "%" + naId + "%",
					"%" + naId + "%", offset, rows, displayTime };
		} else {
			paramArray = new Object[] { offset, rows, displayTime };
		}

		List<Map<String, Object>> result = getJdbcTemplate().query(select,
				paramArray, new ModeratorStreamRowMapper());

		return new ArrayList<Map<String, Object>>(result);
	}

	private List<Map<String, Object>> getTranscriptionStreamSp(int offset,
			int rows, String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("streamOffset", offset);
		inParamMap.put("streamRows", rows);
		inParamMap.put("naId", naId);
		inParamMap.put("displayTime", displayTime);

		List<Map<String, Object>> result = StoredProcedureDataAccessUtils
				.executeGeneric(getJdbcTemplate(), "spGetTranscriptionStream",
						new GenericRowMapper<Map<String, Object>>(
								new ModeratorStreamExtractor()), inParamMap);

		return result;
	}

	@Override
	public int getTranscriptionTotals(int displayTime) {
		return getTranscriptionTotals("", displayTime);
	}

	@Override
	public int getTranscriptionTotals(String naId, int displayTime) {
		return getTranscriptionTotals(naId, displayTime, false);
	}

	@Override
	public int getTranscriptionTotals(String naId, int displayTime,
			boolean useSp) {
		if (!useSp) {
			return getTranscriptionTotalsLegacy(naId, displayTime);
		} else {
			return getTranscriptionTotalsSp(naId, displayTime);
		}
	}

	private int getTranscriptionTotalsLegacy(String naId, int displayTime) {
		String select = "SELECT count(1) AS transcription_count FROM ("
				+ "SELECT annotation_log.na_id, annotation_log.object_id "
				+ "FROM annotation_log "
				+ "JOIN annotation_transcriptions tr ON annotation_log.annotation_id = tr.annotation_id "
				+ "LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id "
				+ "WHERE annotation_log.annotation_type = 'TR' "
				+ "AND "
				+ STATUS_FILTER
				+ "      AND annotation_log.first_annotation_id NOT IN "
				+ "           ("
				+ TRANSCRIPTION_DELETED_FIRST_ANNOTATION_IDS
				+ ") "
				+ (naId != null && !naId.isEmpty() ? "AND "
						+ TRANSCRIPTION_NAID_FILTER : "")
				+ "AND annotation_log.log_ts > date_sub(now(), INTERVAL ? DAY"
				+ ") GROUP BY annotation_log.na_id, annotation_log.object_id) transcription_group;";

		Object[] paramArray = null;

		if (naId != null && !naId.isEmpty()) {
			paramArray = new Object[] { naId, "%" + naId + "%",
					"%" + naId + "%", displayTime };
		} else {
			paramArray = new Object[] { displayTime };
		}

		int result = getJdbcTemplate().queryForInt(select, paramArray);

		return result;
	}

	private int getTranscriptionTotalsSp(String naId, int displayTime) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("displayTime", displayTime);

		int result = StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(), "spGetTranscriptionCount", inParamMap,
				"transcription_count");

		return result;
	}

	@Override
	public List<Map<String, Object>> getModeratorStream(int offset, int rows,
			String naId, int tagDisplayTime, int transcriptionDisplayTime,
			int commentDisplayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		return getModeratorStream(offset, rows, naId, tagDisplayTime,
				transcriptionDisplayTime, commentDisplayTime, false);
	}

	@Override
	public List<Map<String, Object>> getModeratorStream(int offset, int rows,
			String naId, int tagDisplayTime, int transcriptionDisplayTime,
			int commentDisplayTime, boolean useSp) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		if (!useSp) {
			return getModeratorStreamLegacy(offset, rows, naId, tagDisplayTime,
					transcriptionDisplayTime, commentDisplayTime);
		} else {
			return getModeratorStreamSp(offset, rows, naId, tagDisplayTime,
					transcriptionDisplayTime, commentDisplayTime);
		}
	}

	private List<Map<String, Object>> getModeratorStreamLegacy(int offset,
			int rows, String naId, int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {

		String select = MODERATOR_STREAM_SELECT;

		Object[] paramArray;

		if (naId != null && !naId.isEmpty()) {
			paramArray = new Object[] { transcriptionDisplayTime,
					commentDisplayTime, tagDisplayTime, naId, "%" + naId + "%",
					"%" + naId + "%", "%" + naId + "%", "%" + naId + "%",
					offset, rows };
		} else {
			paramArray = new Object[] { transcriptionDisplayTime,
					commentDisplayTime, tagDisplayTime, offset, rows };

		}

		List<Map<String, Object>> result = getJdbcTemplate().query(
				select,
				paramArray,
				new GenericRowMapper<Map<String, Object>>(
						new ModeratorStreamExtractor()));

		return new ArrayList<Map<String, Object>>(result);
	}

	private List<Map<String, Object>> getModeratorStreamSp(int offset,
			int rows, String naId, int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("streamOffset", offset);
		inParamMap.put("streamRows", rows);
		inParamMap.put("naId", naId);
		inParamMap.put("tagDisplayTime", tagDisplayTime);
		inParamMap.put("transcriptionDisplayTime", transcriptionDisplayTime);
		inParamMap.put("commentDisplayTime", commentDisplayTime);

		List<Map<String, Object>> result = StoredProcedureDataAccessUtils
				.executeGeneric(getJdbcTemplate(), "spGetModeratorStream",
						new GenericRowMapper<Map<String, Object>>(
								new ModeratorStreamExtractor()), inParamMap);

		return result;
	}

	@Override
	public int getModeratorTotals(int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime) {
		return getModeratorTotals("", tagDisplayTime, transcriptionDisplayTime,
				commentDisplayTime);
	}

	@Override
	public int getModeratorTotals(String naId, int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime) {
		return getModeratorTotals(naId, tagDisplayTime,
				transcriptionDisplayTime, commentDisplayTime, false);
	}

	@Override
	public int getModeratorTotals(String naId, int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime, boolean useSp) {
		if (!useSp) {
			return getModeratorTotalsLegacy(naId, tagDisplayTime,
					transcriptionDisplayTime, commentDisplayTime);
		} else {
			return getModeratorTotalsSp(naId, tagDisplayTime,
					transcriptionDisplayTime, commentDisplayTime);
		}
	}

	private int getModeratorTotalsLegacy(String naId, int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime) {
		int resultTags = 0;
		int resultTranscriptions = 0;
		int resultComments = 0;

		String select = "SELECT count(1) mod_count from ("
				+ "SELECT annotation_log.na_id, annotation_log.object_id, at.annotation, at.account_id "
				+ "FROM annotation_log "
				+ "JOIN annotation_tags at ON annotation_log.annotation_id = at.annotation_id "
				+ "AND annotation_log.annotation_type = 'TG' "
				+ "LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id "
				+ "WHERE annotation_log.action IN ('REMOVE', 'RESTORE') "
				+ "AND "
				+ STATUS_FILTER
				+ (naId != null && !naId.isEmpty() ? "AND "
						+ MODERATOR_STREAM_TAG_COUNT_NAID_FILTER : "")
				+ "AND annotation_log.annotation_id NOT IN ("
				+ SELECT_DELETED_ANNOTATION_IDS
				+ ") "
				+ "AND annotation_log.log_ts > date_sub(now(), INTERVAL ? DAY) "
				+ "GROUP BY annotation_log.na_id, annotation_log.object_id, at.annotation, at.account_id) mod_stream";

		Object[] paramArray = null;

		if (naId != null && !naId.isEmpty()) {
			paramArray = new Object[] { naId, "%" + naId + "%",
					"%" + naId + "%", tagDisplayTime };
		} else {
			paramArray = new Object[] { tagDisplayTime };
		}

		resultTags = getJdbcTemplate().queryForInt(select, paramArray);

		select = "SELECT count(1) mod_count from ("
				+ "SELECT annotation_log.na_id, annotation_log.object_id "
				+ "FROM annotation_log "
				+ "JOIN annotation_transcriptions tr ON annotation_log.annotation_id = tr.annotation_id "
				+ "LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id "
				+ "AND annotation_log.annotation_type = 'TR' "
				+ "AND "
				+ STATUS_FILTER
				+ "WHERE annotation_log.action IN ('REMOVE', 'RESTORE') "
				+ (naId != null && !naId.isEmpty() ? "AND "
						+ TRANSCRIPTION_NAID_FILTER : "")
				+ "AND annotation_log.log_ts > date_sub(now(), INTERVAL ? DAY) "
				+ "GROUP BY annotation_log.na_id, annotation_log.object_id) mod_stream";

		if (naId != null && !naId.isEmpty()) {
			paramArray = new Object[] { naId, "%" + naId + "%",
					"%" + naId + "%", transcriptionDisplayTime };
		} else {
			paramArray = new Object[] { transcriptionDisplayTime };
		}

		resultTranscriptions = getJdbcTemplate()
				.queryForInt(select, paramArray);

		select = "SELECT count(1) mod_count from ("
				+ "SELECT annotation_log.na_id, annotation_log.object_id "
				+ "FROM annotation_log "
				+ "JOIN annotation_comments cm ON annotation_log.annotation_id = cm.annotation_id "
				+ "LEFT JOIN opa_titles ON annotation_log.na_id = opa_titles.na_id "
				+ "AND annotation_log.annotation_type = 'CM' "
				+ "AND "
				+ STATUS_FILTER
				+ "WHERE annotation_log.action IN ('REMOVE', 'RESTORE') "
				+ (naId != null && !naId.isEmpty() ? "AND "
						+ COMMENT_NAID_FILTER : "")
				+ "AND annotation_log.log_ts > date_sub(now(), INTERVAL ? DAY) "
				+ "GROUP BY annotation_log.na_id, annotation_log.object_id) mod_stream";

		if (naId != null && !naId.isEmpty()) {
			paramArray = new Object[] { naId, "%" + naId + "%",
					"%" + naId + "%", commentDisplayTime };
		} else {
			paramArray = new Object[] { commentDisplayTime };
		}

		resultComments = getJdbcTemplate().queryForInt(select, paramArray);

		return resultTags + resultTranscriptions + resultComments;
	}

	private int getModeratorTotalsSp(String naId, int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("tagDisplayTime", tagDisplayTime);
		inParamMap.put("transcriptionDisplayTime", transcriptionDisplayTime);
		inParamMap.put("commentDisplayTime", commentDisplayTime);

		int result = StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(), "spGetModeratorCount", inParamMap,
				"mod_count");

		return result;
	}

	@Override
	public int getCommentTotals(int displayTime) {
		return getTagTotals(null, displayTime);
	}

	@Override
	public int getCommentTotals(String naId, int displayTime) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId == null || naId.isEmpty() ? null : naId);
		inParamMap.put("timeInterval", displayTime);

		int result = StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(), "spGetCommentCountForModetarorStream",
				inParamMap, "count");

		return result;
	}

	@Override
	public List<Map<String, Object>> getCommentsStream(int offset, int rows,
			String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("streamOffset", offset);
		inParamMap.put("streamRows", rows);
		inParamMap.put("naId", naId);
		inParamMap.put("displayTime", displayTime);

		List<Map<String, Object>> result = StoredProcedureDataAccessUtils
				.executeGeneric(getJdbcTemplate(), "spGetCommentsStream",
						new GenericRowMapper<Map<String, Object>>(
								new ModeratorStreamExtractor()), inParamMap);

		return getFilteredCommentResults(result);
	}

	private List<Map<String, Object>> getFilteredCommentResults(
			List<Map<String, Object>> result) {

		// Filter deleted results
		List<Map<String, Object>> filteredResult = new LinkedList<Map<String, Object>>();
		HashSet<String> deleted = new HashSet<String>();

		for (Map<String, Object> tagMap : result) {

			String key = getKey(tagMap);

			if (tagMap.get("action").toString().equals("DELETE")) {
				// If it was deleted, add to deleted HashSet
				deleted.add(key);
			} else if (!deleted.contains(key)) {
				// Add to filtered results if not deleted and not previous to a
				// delete
				filteredResult.add(tagMap);
			}
		}

		return filteredResult;
	}
}
