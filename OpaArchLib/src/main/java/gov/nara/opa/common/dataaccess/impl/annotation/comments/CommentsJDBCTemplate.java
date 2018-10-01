package gov.nara.opa.common.dataaccess.impl.annotation.comments;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.annotation.comments.CommentsDao;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentsCollectionValueObject;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CommentsJDBCTemplate extends AbstractOpaDbJDBCTemplate implements
	CommentsDao {

	@Override
	public boolean deleteComment(Integer annotationId) {
		return updateCommentStatus(annotationId, false);
	}

	@Override
	public void createComment(CommentValueObject comment)
			throws UnsupportedEncodingException {
		if (comment.getNaId() != null) {
			comment.getNaId().getBytes("UTF-8");
		}

		if (comment.getObjectId() != null) {
			comment.getObjectId().getBytes("UTF-8");
		}

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("comment", comment.getAnnotation());
		inParamMap.put("accountId", comment.getAccountId());
		inParamMap.put("annotationMd5", comment.getAnnotationMD5());
		inParamMap.put("pageNum", comment.getPageNum());
		inParamMap.put("naId", comment.getNaId());
		inParamMap.put("objectId", comment.getObjectId());
		inParamMap.put("opaId", comment.getOpaId());
		inParamMap.put("parentId",
				comment.getParentId() == null ? 0 : comment.getParentId());
		inParamMap.put("commentSequence", comment.getSequence() == null ? 1
				: comment.getSequence());
		inParamMap.put("commentStatus", comment.getStatus());

		int commentId = StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(), "spInsertAnnotationComments", inParamMap,
				"commentId");
		comment.setAnnotationId(new Integer(commentId));
	}

	@Override
	public boolean update(CommentValueObject commObj, String commentText)
			throws DataAccessException, UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationId", commObj.getAnnotationId());
		inParamMap.put("comment", commentText);

		boolean result = StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spUpdateAnnotationComment", inParamMap);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommentsCollectionValueObject selectAllCommentsAndReplies(String naId,
			String objectId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("parentId", null);

		List<CommentValueObject> comments = (List<CommentValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spSelectCommentAndReplies",
						new GenericRowMapper<CommentValueObject>(
								new CommentValueObjectExtractor()), inParamMap);

		List<CommentValueObject> allComments = new ArrayList<CommentValueObject>();
		CommentsCollectionValueObject response = new CommentsCollectionValueObject(allComments);
		if (comments != null && comments.size() > 0) {
			Timestamp lastModified = comments.get(0).getAnnotationTS();
			int removedComments = 0;
			for (CommentValueObject comment : comments) {
				allComments.add(comment);
				if (comment.getAnnotationTS().after(lastModified)) {
					lastModified = comment.getAnnotationTS();
				}
				if (!comment.getStatus()) {
					removedComments++;
				}
				List<CommentValueObject> replies = selectReplies(naId,
						objectId, comment.getAnnotationId());
				comment.setReplies(replies);
				if (replies.size() > 0 && replies.get(0).getAnnotationTS().after(lastModified)) {
					lastModified = replies.get(0).getAnnotationTS();
				}
			}
			response = new CommentsCollectionValueObject(allComments);
			response.setLastModified(lastModified);
			response.setTotalRemovedComments(removedComments);			
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private List<CommentValueObject> selectReplies(String naId,
			String objectId, Integer parentId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("parentId", parentId);

		List<CommentValueObject> replies = (List<CommentValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spSelectCommentAndReplies",
						new GenericRowMapper<CommentValueObject>(
								new CommentValueObjectExtractor()), inParamMap);

		return replies;
	}

	@Override
	public CommentValueObject selectCommentAndReplies(Integer annotationId,
			String naId, String objectId) {

		CommentValueObject theComment = getCommentByAnnotationId(annotationId);
		if (theComment != null) {
			List<CommentValueObject> replies = selectReplies(naId, objectId,
					theComment.getAnnotationId());
			theComment.setReplies(replies);
		}

		return theComment;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommentValueObject getLatestReply(Integer parentId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("parentId", parentId);

		List<CommentValueObject> comments = (List<CommentValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetLastReply",
						new GenericRowMapper<CommentValueObject>(
								new CommentValueObjectExtractor()), inParamMap);

		CommentValueObject reply = null;
		if (comments != null && comments.size() > 0) {
			reply = comments.get(0);
		}

		return reply;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommentValueObject getCommentByAnnotationId(Integer annotationId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationId", annotationId);

		List<CommentValueObject> comments = (List<CommentValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spSelectComment",
						new GenericRowMapper<CommentValueObject>(
								new CommentValueObjectExtractor()), inParamMap);

		CommentValueObject theComment = null;
		if (comments != null && comments.size() > 0) {
			theComment = comments.get(0);
		}

		return theComment;
	}

	@Override
	public boolean updateCommentStatus(Integer annotationId, Boolean status) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationId", annotationId.intValue());
		inParamMap.put("commentStatus", status);

		return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spUpdateAnnotationCommentStatusById", inParamMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommentValueObject getReplyByAnnotationId(Integer annotationId,
			Integer parentId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationId", annotationId.intValue());
		inParamMap.put("parentId", parentId);
		// Send parentId to make sure it is a reply

		List<CommentValueObject> comments = (List<CommentValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationCommentById",
						new GenericRowMapper<CommentValueObject>(
								new CommentValueObjectExtractor()), inParamMap);

		CommentValueObject comment = null;
		if (comments != null && comments.size() > 0) {
			comment = comments.get(0);
		}

		return comment;
	}
}
