package gov.nara.opa.common.dataaccess.impl.annotation.tags;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.valueobject.ValueObjectUtils;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;
import gov.nara.opa.common.valueobject.annotation.tags.Tag;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObjectConstants;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TagJDBCTemplate extends AbstractOpaDbJDBCTemplate implements
		TagDao, TagValueObjectConstants {

	OpaLogger logger = OpaLogger.getLogger(TagJDBCTemplate.class);

	private static final String SELECT_TAGS_BASE = "SELECT AT.ANNOTATION_ID, AT.ANNOTATION, AT.ANNOTATION_MD5, AT.STATUS, "
			+ "AT.NA_ID, AT.OBJECT_ID, AT.PAGE_NUM, AT.OPA_ID, AT.ACCOUNT_ID, AT.ANNOTATION_TS, A.USER_NAME, "
			+ "A.FULL_NAME, A.IS_NARA_STAFF, A.DISPLAY_NAME_FLAG "
			+ "FROM annotation_tags AT, accounts A "
			+ "WHERE (AT.STATUS = 0 OR AT.STATUS = 1) AND AT.ACCOUNT_ID = A.ACCOUNT_ID AND ";

	private static final String TAGS_LOG_ACTION_QUALIFIER_SQL = " AND EXISTS (SELECT 1 FROM annotation_log AL WHERE AT.ANNOTATION_ID = AL.ANNOTATION_ID AND AL.ACTION = ?)";

	private static final String SELECT_TAGS_BY_ANNOTATION_ID_SQL = SELECT_TAGS_BASE
			+ "AND AT.ANNOTATION_ID = ?";

	private static final String SELECT_TAGS_TEXT_NAID_SQL = "SELECT ANNOTATION FROM annotation_tags WHERE STATUS = ? AND NA_ID = ? AND OBJECT_ID IS NULL";
	private static final String SELECT_TAGS_TEXT_NAID_AND_OBJECT_ID_SQL = "SELECT ANNOTATION FROM annotation_tags WHERE "
			+ "STATUS = ? AND NA_ID = ? AND OBJECT_ID = ?";

	private static final String ORDER_BY_TAG_TEXT = " ORDER BY annotation ";

	private static final String ORDER_BY_TAG_ID_DESC = " ORDER BY annotation_id DESC ";
	private static final String ORDER_BY_TAG_ID_ASC = " ORDER BY annotation_id ";
	private static final String ORDER_BY_TS_DESC = " ORDER BY annotation_ts DESC ";

	private static final String INSERT_TAG_SQL;
	static {
		List<String> ignoreFields = new ArrayList<String>();
		ignoreFields.add(ANNOTATION_ID_DB);
		INSERT_TAG_SQL = ValueObjectUtils.createInsertStatement(
				"INSERT INTO annotation_tags", TagValueObjectConstants.class,
				ignoreFields);
	}

	private static final String UPDATE_TAG_STATUS_SQL = "UPDATE annotation_tags SET STATUS = ? WHERE ANNOTATION_ID = ?";

	@Override
	public Tag select(int annotationId) throws DataAccessException,
			UnsupportedEncodingException {

		String sql = "SELECT at.annotation_id, at.annotation, "
				+ "at.annotation_md5, at.status, at.na_id, "
				+ "at.object_id, at.page_num, at.opa_id, "
				+ "at.account_id, at.annotation_ts, a.user_name, "
				+ "a.full_name, a.is_nara_staff FROM annotation_tags at, "
				+ "accounts a WHERE (at.status = 0 OR at.status = 1) AND at.annotation_id = ? "
				+ "AND at.account_id = a.account_id";

		List<Tag> tags = getJdbcTemplate().query(sql,
				new Object[] { annotationId }, new TagRowMapper());

		if (tags != null && tags.size() > 0) {
			return tags.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<Tag> select(String naId, String objectId, String tagText,
			int status, boolean md5Flag) throws DataAccessException,
			UnsupportedEncodingException {
		String fieldName = "annotation";
		if (md5Flag) {
			fieldName = "annotation_md5";
			tagText = DigestUtils.md5Hex(tagText);
		}

		// Retrieve the tag information
		String sql = "SELECT at.annotation_id, at.annotation, "
				+ "at.annotation_md5, at.status, at.na_id, "
				+ "at.object_id, at.page_num, at.opa_id, "
				+ "at.account_id, at.annotation_ts, a.user_name, "
				+ "a.full_name, a.is_nara_staff FROM annotation_tags at, "
				+ "accounts a WHERE at." + fieldName + " = ? AND "
				+ "at.na_id = ? AND at.object_id = ? AND at.status = ? "
				+ "AND at.account_id = a.account_id " + ORDER_BY_TAG_TEXT;

		return getJdbcTemplate().query(
				sql,
				new Object[] { tagText.getBytes("UTF-8"),
						naId.getBytes("UTF-8"), objectId.getBytes("UTF-8"),
						status }, new TagRowMapper());

	}

	@Override
	public List<TagValueObject> selectAllTagsByNaIds(String[] naIdsList)
			throws DataAccessException, UnsupportedEncodingException {
		String sql = " SELECT at.annotation_id, at.annotation, "
				+ " at.annotation_md5, at.status, at.na_id, "
				+ " at.object_id, at.page_num, at.opa_id, "
				+ " at.account_id, at.annotation_ts, a.user_name, a.display_name_flag, "
				+ " a.full_name, a.is_nara_staff FROM annotation_tags at, "
				+ " accounts a WHERE at.na_id in ( ";

		for (int i = 0; i < naIdsList.length; i++) {
			sql += "'" + naIdsList[i] + "'";
			if (i < naIdsList.length - 1)
				sql += ",";
		}

		sql += " ) AND at.account_id = a.account_id AND at.status = 1 GROUP BY at.annotation, at.na_id ";

		return getJdbcTemplate().query(sql.toString(), new Object[] {},
				new TagValueObjectRowMapper());

	}

	@Override
	public List<OpaTitle> getTitlesByNaIds(String[] naIdsList)
			throws DataAccessException, UnsupportedEncodingException {

		String sql = " SELECT at.annotation, at.page_num, at.na_id, at.object_id, at.account_id, "
				+ "at.annotation_ts, CONVERT(ot.opa_title USING 'UTF8') opa_title, ot.opa_type, ot.total_pages, a.user_name, a.full_name, "
				+ "a.is_nara_staff "
				+ "FROM annotation_tags at "
				+ "JOIN accounts a ON at.account_id = a.account_id "
				+ "LEFT JOIN opa_titles ot ON ot.na_id = at.na_id "
				+ "WHERE at.status = 1 AND ot.na_id in ( ";

		for (int i = 0; i < naIdsList.length; i++) {
			sql += "'" + naIdsList[i] + "'";
			if (i < naIdsList.length - 1)
				sql += ",";
		}

		sql += " ) group by ot.na_id ";

		List<OpaTitle> opaTitles = getJdbcTemplate().query(sql,
				new Object[] {}, new OpaTitleRowMapper());

		if (opaTitles != null && opaTitles.size() > 0) {
			return opaTitles;
		} else {
			return null;
		}

	}

	@Override
	public List<TagValueObject> selectAllTags(String naId, String objectId,
			String text, Boolean status) {
		return selectAllTags(naId, objectId, text, status, null);
	}

	@Override
	public List<TagValueObject> selectAllTags(String naId, String objectId,
			String text, Boolean status, String action) {
		return selectAllTags(naId, objectId, text, status, action, false);
	}

	@Override
	public List<TagValueObject> selectAllTags(String naId, String objectId,
			String text, Boolean status, String action, boolean descendingOrder) {

		if (objectId != null && objectId.trim().equals("")) {
			objectId = null;
		}

		if (text != null && text.trim().equals("")) {
			objectId = null;
		}

		// Retrieve the tag information
		StringBuffer sql = new StringBuffer(SELECT_TAGS_BASE);
		ArrayList<Object> params = new ArrayList<Object>();

		appendAndParamToSql("AT." + NA_ID_DB, naId, sql, params);
		appendAndParamToSql("AT." + OBJECT_ID_DB, objectId, sql, params, false,
				true);
		appendAndParamToSql("AT." + ANNOTATION_DB, text, sql, params);
		appendAndParamToSql("AT." + STATUS_DB, status, sql, params);

		if (action != null) {
			sql.append(TAGS_LOG_ACTION_QUALIFIER_SQL);
			params.add(action);
		}

		// Add order clause
		if (descendingOrder) {
			sql.append(ORDER_BY_TS_DESC);
		} else {
			sql.append(ORDER_BY_TAG_TEXT);
		}

		return getJdbcTemplate().query(sql.toString(), params.toArray(),
				new TagValueObjectRowMapper());
	}

	@Override
	public List<TagValueObject> selectAllTagsByAnnotationId(String annotationId) {
		return getJdbcTemplate().query(
				SELECT_TAGS_BY_ANNOTATION_ID_SQL + ORDER_BY_TAG_TEXT,
				new Object[] { annotationId }, new TagValueObjectRowMapper());
	}

	@Override
	public Tag update(Tag tag) throws DataAccessException,
			UnsupportedEncodingException {

		// Set the restored tag annotation status to 1
		String sql = "UPDATE annotation_tags SET " + "status = ? "
				+ "WHERE annotation_id = ?";

		getJdbcTemplate().update(sql,
				new Object[] { tag.isStatus(), tag.getAnnotationId() });

		return select(tag.getAnnotationId());
	}

	@Override
	public void updateTagStatus(Integer annotationId, Boolean status) {
		getJdbcTemplate().update(UPDATE_TAG_STATUS_SQL,
				new Object[] { status, annotationId });
	}

	@Override
	public List<String> selectTagValuesByNaIdAndObjectId(String naId,
			String objectId, Boolean status) {
		String sql = null;
		List<Object> params = new ArrayList<Object>();
		params.add(status);
		params.add(naId);
		if (objectId == null) {
			sql = SELECT_TAGS_TEXT_NAID_SQL;
		} else {
			sql = SELECT_TAGS_TEXT_NAID_AND_OBJECT_ID_SQL;
			params.add(objectId);
		}

		// Order clause
		sql += ORDER_BY_TAG_TEXT;

		List<String> tagsValues = getJdbcTemplate().queryForList(sql,
				String.class, params.toArray());
		return tagsValues;
	}

	@Override
	public void createTag(TagValueObject tag) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getNamedJdbcTemplate().update(INSERT_TAG_SQL,
				new MapSqlParameterSource(tag.getDatabaseContent()), keyHolder);
		int tagId = keyHolder.getKey().intValue();
		tag.setAnnotationId(new Integer(tagId));
	}

	@Override
	public List<String> selectTagValuesByNaIdAndObjectId(String naId,
			String objectId) {
		return selectTagValuesByNaIdAndObjectId(naId, objectId, null);
	}

	@Override
	public List<OpaTitle> selectTaggedTitles(String tagText, String title,
			String userName, int offset, int rows) throws DataAccessException,
			UnsupportedEncodingException {
		return selectTaggedTitles(tagText, title, userName, offset, rows, true);
	}

	@Override
	public List<OpaTitle> selectTaggedTitles(String tagText, String title,
			String userName, int offset, int rows, boolean descOrder)
			throws DataAccessException, UnsupportedEncodingException {

		String sql = " SELECT at.annotation, at.page_num, at.na_id, at.object_id, at.account_id, "
				+ "at.annotation_ts, CONVERT(ot.opa_title USING 'UTF8') opa_title, ot.opa_type, ot.total_pages, a.user_name, a.full_name, "
				+ "a.is_nara_staff "
				+ "FROM annotation_tags at "
				+ "JOIN accounts a ON at.account_id = a.account_id "
				+ "LEFT JOIN opa_titles ot ON ot.na_id = at.na_id "
				+ "WHERE at.status = 1 "
				+ "AND a.user_name = ? "
				+ "AND at.annotation = ? ";

		if (title != null && !title.equals("")) {
			sql += String
					.format("  AND (at.na_id = '%1$s' OR convert(ot.opa_title using UTF8) LIKE '%%%1$s%%') ",
							title);
		}

		// Order clause
		if (descOrder) {
			sql += ORDER_BY_TAG_ID_DESC;
		} else {
			sql += ORDER_BY_TAG_ID_ASC;
		}

		// Add the LIMIT configuration
		sql += " LIMIT ?, ? ";

		List<OpaTitle> opaTitles = getJdbcTemplate().query(sql,
				new Object[] { userName, tagText, offset, rows },
				new OpaTitleRowMapper());

		if (opaTitles != null && opaTitles.size() > 0) {
			return opaTitles;
		} else {
			return null;
		}
	}

	@Override
	public int selectTaggedTitleCount(String tagText, String title,
			String userName) throws DataAccessException,
			UnsupportedEncodingException {

		String sql = " SELECT COUNT(1) " + "FROM annotation_tags at "
				+ "JOIN accounts a ON at.account_id = a.account_id "
				+ "LEFT JOIN opa_titles ot ON ot.na_id = at.na_id "
				+ "WHERE at.status = 1 " + "AND a.user_name = ? "
				+ "AND at.annotation = ? ";

		if (!StringUtils.isNullOrEmtpy(title)) {
			sql += String
					.format("  AND (at.na_id = '%1$s' OR convert(ot.opa_title using UTF8) LIKE '%%%1$s%%') ",
							title);
		}

		int titleCount = getJdbcTemplate().queryForInt(sql,
				new Object[] { userName, tagText });

		return titleCount;
	}
}
