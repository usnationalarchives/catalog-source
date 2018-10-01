package gov.nara.opa.common.dataaccess.impl.annotation.tags;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.annotation.tags.Tag;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class TagResultSetExtractor implements ResultSetExtractor<Tag> {

	private static OpaLogger logger = OpaLogger
			.getLogger(TagResultSetExtractor.class);

	@Override
	public Tag extractData(ResultSet rs) throws SQLException {
		Tag tagObj = new Tag();

		try {
			tagObj.setAnnotationId(rs.getInt(1));
			if (rs.getString(2) != null) {
				tagObj.setAnnotation(new String(rs.getBytes(2), "UTF-8"));
			}
			if (rs.getString(3) != null) {
				tagObj.setAnnotationMD5(new String(rs.getBytes(3), "UTF-8"));
			} else {
				tagObj.setAnnotationMD5("");
			}
			tagObj.setStatus(rs.getBoolean(4));
			if (rs.getString(5) != null) {
				tagObj.setNaId(new String(rs.getBytes(5), "UTF-8"));
			} else {
				return null;
			}
			if (rs.getString(6) != null) {
				tagObj.setObjectId(new String(rs.getBytes(6), "UTF-8"));
			} else {
				tagObj.setObjectId("");
			}
			tagObj.setPageNum(rs.getInt(7));
			if (rs.getString(8) != null) {
				tagObj.setOpaId(new String(rs.getBytes(8), "UTF-8"));
			} else {
				tagObj.setOpaId("");
			}
			tagObj.setAccountId(rs.getInt(9));
			tagObj.setAnnotationTS(rs.getTimestamp(10));
			if (rs.getString(11) != null) {
				tagObj.setUserName(new String(rs.getBytes(11), "UTF-8"));
			} else {
				return null;
			}
			if (rs.getString(12) != null) {
				tagObj.setFullName(new String(rs.getBytes(12), "UTF-8"));
			} else {
				tagObj.setFullName("");
			}
			tagObj.setIsNaraStaff(rs.getBoolean(13));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		}

		return tagObj;
	}

}
