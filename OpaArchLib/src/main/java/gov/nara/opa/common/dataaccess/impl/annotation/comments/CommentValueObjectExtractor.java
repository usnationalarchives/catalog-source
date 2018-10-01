package gov.nara.opa.common.dataaccess.impl.annotation.comments;

import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObjectConstants;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class CommentValueObjectExtractor implements
		ResultSetExtractor<CommentValueObject>,
		UserAccountValueObjectConstants, CommentValueObjectConstants {

	@Override
	public CommentValueObject extractData(ResultSet rs) throws SQLException {
		// comment specific fields
		CommentValueObject comment = new CommentValueObject();
		comment.setAnnotationId(rs.getInt(ANNOTATION_ID_DB));
		comment.setAnnotation(rs.getString(ANNOTATION_DB));
		comment.setAccountId(rs
				.getInt(CommentValueObjectConstants.ACCOUNT_ID_DB));
		comment.setAnnotationMD5(rs.getString(ANNOTATION_MD5_DB));
		comment.setAnnotationTS(rs.getTimestamp(ANNOTATION_TS_DB));
		comment.setAnnotationCreatedTS(rs
				.getTimestamp(ANNOTATION_CREATED_TS_DB));
		comment.setNaId(rs.getString(NA_ID_DB));
		comment.setObjectId(rs.getString(OBJECT_ID_DB));
		comment.setOpaId(rs.getString(OPA_ID_DB));
		comment.setParentId(rs.getInt(PARENT_ID_DB));
		comment.setSequence(rs.getInt(SEQUENCE_DB));
		comment.setStatus(rs.getBoolean(STATUS_DB));

		// account related fields
		comment.setUserName(rs.getString(USER_NAME_DB));
		comment.setFullName(rs.getString(FULL_NAME_DB));
		comment.setIsNaraStaff(rs.getBoolean(IS_NARA_STAFF_DB));
		comment.setDisplayNameFlag(rs.getBoolean(DISPLAY_NAME_FLAG_DB));

		return comment;
	}
}
