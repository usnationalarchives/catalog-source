package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ModeratorStreamExtractor implements
		ResultSetExtractor<Map<String, Object>> {

	private static OpaLogger logger = OpaLogger
			.getLogger(ModeratorStreamExtractor.class);

	@Override
	public Map<String, Object> extractData(ResultSet resultSet)
			throws SQLException, DataAccessException {

		HashMap<String, Object> map = new HashMap<String, Object>();
		try {

			map.put("logId", resultSet.getInt("log_id"));
			String annotationType = resultSet.getString("annotation_type");
			map.put("annotationType", annotationType);
			map.put("annotationId", resultSet.getInt("annotation_id"));
			map.put("TS", resultSet.getTimestamp("log_ts"));
			map.put("userName", resultSet.getString("user_name"));
			map.put("fullName", resultSet.getString("full_name"));
			map.put("displayFullName",
					resultSet.getBoolean("display_name_flag"));
			map.put("isNaraStaff", resultSet.getBoolean("is_nara_staff"));
			map.put("authorUserName", resultSet.getString("author_user_name"));
			map.put("authorFullName", resultSet.getString("author_full_name"));
			map.put("authorDisplayFullName",
					resultSet.getBoolean("author_display_name_flag"));
			map.put("authorIsNaraStaff",
					resultSet.getBoolean("author_is_nara_staff"));
			map.put("action", resultSet.getString("action"));

			String notes = resultSet.getString("notes");
			map.put("hasNote", (notes != null ? true : false));
			map.put("notes", (notes != null ? notes : ""));

			map.put("title", resultSet.getString("opa_title"));
			map.put("on", resultSet.getString("opa_type"));
			map.put("totalPages", resultSet.getInt("total_pages"));
			map.put("naId", resultSet.getString("na_id"));
			map.put("pageNum", resultSet.getString("page_num"));

			String reason = resultSet.getString("reason");
			map.put("reason", (reason != null ? reason : ""));

			switch (annotationType) {
			case "TG":
				map.put("text", new String(resultSet.getBytes("tag_text"),
						"UTF-8"));
				map.put("objectId", resultSet.getString("object_id"));
				map.put("createTS", resultSet.getTimestamp("TG_ts"));
				break;
			case "CM":
				map.put("text", new String(resultSet.getBytes("comment_text"),
						"UTF-8"));
				map.put("objectId", resultSet.getString("object_id"));
				map.put("createTS", resultSet.getTimestamp("CM_ts"));
				break;
			case "TR":
				String teaser = new String(
						resultSet.getBytes("transcription_teaser"), "UTF-8");

				map.put("teaser",
						(teaser != null && !teaser.isEmpty() ? (teaser.length() > 200 ? teaser
								.substring(0, 200) : teaser)
								: ""));
				map.put("objectId", resultSet.getString("object_id"));
				map.put("version", resultSet.getInt("version_num"));
				map.put("createTS", resultSet.getTimestamp("TR_ts"));
				break;
			case "AH":
				map.put("createTS", resultSet.getTimestamp("log_ts"));
				break;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new InvalidDataAccessApiUsageException(e.getMessage());
		}

		return map;
	}

}
