package gov.nara.opa.common.dataaccess.annotation.comments;

import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentsCollectionValueObject;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;

public interface CommentsDao {

	boolean updateCommentStatus(Integer annotationId, Boolean status);

	void createComment(CommentValueObject comment)
			throws UnsupportedEncodingException;

	boolean deleteComment(Integer annotationId);

	public boolean update(CommentValueObject commObj, String commentText)
			throws DataAccessException, UnsupportedEncodingException;

	CommentsCollectionValueObject selectAllCommentsAndReplies(String naId,
			String objectId);

	CommentValueObject selectCommentAndReplies(Integer annotationId,
			String naId, String objectId);

	CommentValueObject getLatestReply(Integer parentId);

	CommentValueObject getReplyByAnnotationId(Integer annotationId,
			Integer parentId);

	CommentValueObject getCommentByAnnotationId(Integer annotationId);
}
