package gov.nara.opa.api.dataaccess.impl.user.contributions;

import gov.nara.opa.api.valueobject.user.contributions.UserContributedCommentValueObject;
import gov.nara.opa.api.valueobject.user.contributions.UserContributedCommentValueObjectConstants;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class UserContributedCommentExtractor implements
		ResultSetExtractor<UserContributedCommentValueObject>,
		UserAccountValueObjectConstants,
		UserContributedCommentValueObjectConstants {

	@Override
	public UserContributedCommentValueObject extractData(ResultSet resultSet)
			throws SQLException, DataAccessException {

		UserContributedCommentValueObject userContributedComment = new UserContributedCommentValueObject();

		userContributedComment.setAnnotationId(resultSet
				.getInt(ANNOTATION_ID_DB));
		userContributedComment
				.setAnnotation(resultSet.getString(ANNOTATION_DB));
		userContributedComment.setStatus(resultSet.getBoolean(STATUS_DB));
		userContributedComment.setNaId(resultSet.getString(NA_ID_DB));
		userContributedComment.setObjectId(resultSet.getString(OBJECT_ID_DB));
		userContributedComment.setPageNum(resultSet.getInt(PAGE_NUM_DB));
		userContributedComment.setOpaId(resultSet.getString(OPA_ID_DB));
		userContributedComment
				.setAccountId(resultSet
						.getInt(UserContributedCommentValueObjectConstants.ACCOUNT_ID_DB));
		userContributedComment.setAnnotationTS(resultSet
				.getTimestamp(ANNOTATION_TS_DB));
		userContributedComment.setParentId(resultSet.getInt(PARENT_ID_DB));
		userContributedComment.setReplies(resultSet.getInt(REPLIES_DB));

		userContributedComment.setType(resultSet.getString(OPA_TYPE_DB));
		userContributedComment.setTitle(resultSet.getString(OPA_TITLE_DB));
		userContributedComment.setTotalPages(resultSet.getInt(TOTAL_PAGES_DB));

		userContributedComment.setUserName(resultSet.getString(USER_NAME_DB));
		userContributedComment.setFullName(resultSet.getString(FULL_NAME_DB));
		userContributedComment.setIsNaraStaff(resultSet
				.getBoolean(IS_NARA_STAFF_DB));
		userContributedComment.setDisplayNameFlag(resultSet
				.getBoolean(DISPLAY_NAME_FLAG_DB));

		return userContributedComment;
	}
}