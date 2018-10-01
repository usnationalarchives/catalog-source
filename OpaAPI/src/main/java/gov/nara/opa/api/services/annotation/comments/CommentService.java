package gov.nara.opa.api.services.annotation.comments;

import gov.nara.opa.api.validation.annotation.comments.CommentsCreateRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsAndRepliesRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsViewRequestParameters;
import gov.nara.opa.common.validation.moderator.CommentsModeratorRequestParameters;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentsCollectionValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface CommentService {

	CommentValueObject createComment(
			CommentsCreateRequestParameters commentsRequest)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException;

	CommentValueObject createComment(UserAccountValueObject userAccount,
			String naId, String objectId, String text, Integer pageNum,
			String sessionId);

	CommentValueObject replyComment(
			CommentsCreateRequestParameters commentsRequest)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException;

	CommentValueObject replyComment(CommentValueObject parentComment,
			UserAccountValueObject userAccount, String naId, String objectId,
			String text, Integer pageNum, String sessionId);

	void modifyComment(CommentValueObject comment,
			String text, String sessionId)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException;

	void modifyReply(CommentValueObject comment, Integer replyId,
			CommentsAndRepliesRequestParameters requestParameters, String sessionId)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException;

	void deleteComment(CommentValueObject comment, String sessionId)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException;

	void deleteReply(CommentValueObject comment, Integer replyId,
			String sessionId) throws BadSqlGrammarException,
			DataAccessException, UnsupportedEncodingException;

	CommentValueObject viewCommentById(Integer commentId, String naId,
			String objectId);

	CommentsCollectionValueObject getComments(
			CommentsViewRequestParameters requestParameters);

	void removeComment(CommentsModeratorRequestParameters requestParameters,
			CommentValueObject comment, String sessionId)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException;

	void restoreComment(CommentsModeratorRequestParameters requestParameters,
			CommentValueObject comment, String sessionId)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException;
}
