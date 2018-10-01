package gov.nara.opa.api.services.impl.annotation.comments;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.annotation.comments.CommentService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.validation.annotation.comments.CommentsAndRepliesRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsCreateRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsViewRequestParameters;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.dataaccess.annotation.comments.CommentsDao;
import gov.nara.opa.common.validation.moderator.CommentsModeratorRequestParameters;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentsCollectionValueObject;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObjectHelper;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

@Component
@Transactional
public class CommentServiceImpl implements CommentService {

	private static OpaLogger log = OpaLogger
			.getLogger(CommentServiceImpl.class);

	@Autowired
	private CommentsDao commentsDao;

	@Autowired
	private AnnotationLogDao annotationLogDao;

	@Autowired
	private CommentsHelper commentsHelper;
	
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public CommentValueObject createComment(
			CommentsCreateRequestParameters commentsRequest)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		// Get session user account
		UserAccountValueObject userAccount = commentsHelper
				.getSessionUserAccount();
		CommentValueObject comment = createComment(userAccount,
				commentsRequest.getNaId(), commentsRequest.getObjectId(),
				commentsRequest.getText(), commentsRequest.getPageNum(),
				commentsRequest.getHttpSessionId());
		return comment;
	}

	public CommentValueObject createComment(UserAccountValueObject userAccount,
			String naId, String objectId, String text, Integer pageNum,
			String sessionId) {
		CommentValueObject comment = null;
		try {

			String commentText = StringUtils.replaceMultipleWhiteSpaces(text
					.trim());

			log.info(String
					.format("Comment text in service: %1$s", commentText));

			comment = commentsHelper.createCommentForInsert(
					userAccount.getAccountId(), naId, objectId, commentText,
					pageNum);
			commentsDao.createComment(comment);
			comment = commentsDao.getCommentByAnnotationId(comment
					.getAnnotationId());
			comment.setUserName(userAccount.getUserName());
			comment.setFullName(userAccount.getFullName());
			comment.setIsNaraStaff(userAccount.isNaraStaff());
			comment.setDisplayNameFlag(userAccount.getDisplayFullName());

			AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
					.createAnnotationLogForInsert(comment, sessionId,
							CommonValueObjectConstants.ACTION_NEW);
			annotationLog.setFirstAccountId(userAccount.getAccountId());
			annotationLog.setAffectsAccountId(userAccount.getAccountId());
			annotationLog.setSequence(1);
			annotationLog.setParentId(0);
			annotationLogDao.insert(annotationLog);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return comment;
	}

	@Override
	public CommentValueObject replyComment(
			CommentsCreateRequestParameters commentsRequest)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		Integer commentId = commentsRequest.getAnnotationId();
		CommentValueObject comment = null;

		if (commentId != null) {

			UserAccountValueObject userAccount = commentsHelper
					.getSessionUserAccount();
			CommentValueObject baseComment = commentsDao
					.selectCommentAndReplies(commentId,
							commentsRequest.getNaId(),
							commentsRequest.getObjectId());

			comment = replyComment(baseComment, userAccount,
					commentsRequest.getNaId(), commentsRequest.getObjectId(),
					commentsRequest.getText(), commentsRequest.getPageNum(),
					commentsRequest.getHttpSessionId());
		}

		return comment;
	}

	public CommentValueObject replyComment(CommentValueObject parentComment,
			UserAccountValueObject userAccount, String naId, String objectId,
			String text, Integer pageNum, String sessionId) {
		CommentValueObject comment = null;
		try {

			String replyText = StringUtils.replaceMultipleWhiteSpaces(text
					.trim());

			log.info(String.format("Reply text in service: %1$s", replyText));

			comment = commentsHelper.createCommentForInsert(
					userAccount.getAccountId(), naId, objectId, replyText,
					pageNum);
			comment.setParentId(parentComment.getAnnotationId());

			CommentValueObject latestReply = commentsDao.getLatestReply(comment
					.getParentId());

			if (latestReply != null) {
				comment.setSequence(latestReply.getSequence() + 1);
			} else {
				comment.setSequence(1);
			}

			commentsDao.createComment(comment);
			comment = commentsDao.getCommentByAnnotationId(comment
					.getAnnotationId());
			comment.setUserName(userAccount.getUserName());
			comment.setFullName(userAccount.getFullName());
			comment.setIsNaraStaff(userAccount.isNaraStaff());
			comment.setDisplayNameFlag(userAccount.getDisplayFullName());

			AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
					.createAnnotationLogForInsert(comment, sessionId,
							CommonValueObjectConstants.ACTION_REPLY);

			annotationLog.setAnnotationId(comment.getAnnotationId());
			annotationLog.setParentId(comment.getParentId());
			if (latestReply != null) {
				annotationLog.setSequence(latestReply.getSequence() + 1);
			} else {
				annotationLog.setSequence(1);
			}

			annotationLog.setAffectsAccountId(parentComment.getAccountId());
			annotationLog.setFirstAccountId(parentComment.getAccountId());
			annotationLogDao.insert(annotationLog);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return comment;
	}

	@Override
	public void modifyComment(CommentValueObject comment,
			String text, String sessionId) throws BadSqlGrammarException,
			DataAccessException, UnsupportedEncodingException {

		boolean result = commentsDao.update(comment,
				text);
		if (result) {
			comment.setAnnotation(text);
			comment = commentsDao.getCommentByAnnotationId(comment
					.getAnnotationId());
		}

		try {
			annotationLogDao.disableByAnnotationId(comment.getAnnotationId(),
					AnnotationConstants.ANNOTATION_TYPE_COMMENT);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
				.createAnnotationLogForInsert(comment, sessionId,
						CommonValueObjectConstants.ACTION_EDIT);
		annotationLog.setFirstAccountId(comment.getAccountId());
		annotationLog.setAffectsAccountId(comment.getAccountId());
		annotationLog.setParentId(comment.getParentId());
		annotationLog.setAnnotationMD5(null);
		annotationLog.setSequence(comment.getSequence());
		annotationLog.setStatus(true);
		annotationLogDao.insert(annotationLog);
	}

	@Override
	public void modifyReply(CommentValueObject comment, Integer replyId,
			CommentsAndRepliesRequestParameters requestParameters,
			String sessionId) throws BadSqlGrammarException,
			DataAccessException, UnsupportedEncodingException {

		if (comment != null && comment.getAnnotationId() != null
				&& comment.getAnnotationId().intValue() == replyId.intValue()) {
			boolean result = commentsDao.update(comment,
					requestParameters.getText());
			if (result) {
				comment.setAnnotation(requestParameters.getText());
				comment = commentsDao.getCommentByAnnotationId(replyId);
			}

			try {
				annotationLogDao.disableByAnnotationId(
						comment.getAnnotationId(),
						AnnotationConstants.ANNOTATION_TYPE_COMMENT);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
					.createAnnotationLogForInsert(comment, sessionId,
							CommonValueObjectConstants.ACTION_EDIT);
			annotationLog.setFirstAccountId(comment.getAccountId());
			annotationLog.setAffectsAccountId(comment.getAccountId());
			annotationLog.setParentId(comment.getParentId());
			annotationLog.setAnnotationMD5(null);
			annotationLog.setSequence(comment.getSequence());
			annotationLog.setStatus(true);
			annotationLogDao.insert(annotationLog);
		}
	}

	@Override
	public void deleteComment(CommentValueObject comment, String sessionId)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		commentsDao.deleteComment(comment.getAnnotationId());
		comment.setStatus(false);

		try {
			annotationLogDao.disableByAnnotationId(comment.getAnnotationId(),
					AnnotationConstants.ANNOTATION_TYPE_COMMENT);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
				.createAnnotationLogForInsert(comment, sessionId,
						CommonValueObjectConstants.ACTION_DELETE);
		annotationLog.setFirstAccountId(comment.getAccountId());
		annotationLog.setAffectsAccountId(comment.getAccountId());
		annotationLog.setParentId(comment.getParentId());
		annotationLog.setAnnotationMD5(null);
		annotationLog.setSequence(comment.getSequence());
		annotationLog.setStatus(true);
		annotationLogDao.insert(annotationLog);
	}

	@Override
	public void deleteReply(CommentValueObject comment, Integer replyId,
			String sessionId) throws BadSqlGrammarException,
			DataAccessException, UnsupportedEncodingException {

		if (comment != null && comment.getAnnotationId() != null
				&& comment.getAnnotationId().intValue() == replyId.intValue()) {
			commentsDao.deleteComment(comment.getAnnotationId());
			comment.setStatus(false);

			try {
				annotationLogDao.disableByAnnotationId(
						comment.getAnnotationId(),
						AnnotationConstants.ANNOTATION_TYPE_COMMENT);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
					.createAnnotationLogForInsert(comment, sessionId,
							CommonValueObjectConstants.ACTION_DELETE);
			annotationLog.setFirstAccountId(comment.getAccountId());
			annotationLog.setAffectsAccountId(comment.getAccountId());
			annotationLog.setParentId(comment.getParentId());
			annotationLog.setAnnotationMD5(null);
			annotationLog.setSequence(comment.getSequence());
			annotationLog.setStatus(true);
			annotationLogDao.insert(annotationLog);
		}
	}

	@Override
	public CommentValueObject viewCommentById(Integer commentId, String naId,
			String objectId) {
		CommentValueObject comment = commentsDao.selectCommentAndReplies(
				commentId, naId, objectId);
		return comment;
	}

	@Override
	public CommentsCollectionValueObject getComments(
			CommentsViewRequestParameters commentsParameters) {
		CommentsCollectionValueObject comments = commentsDao
				.selectAllCommentsAndReplies(commentsParameters.getNaId(),
						commentsParameters.getObjectId());
		comments.setCommentsFormat(configurationService.getConfig().getCommentsFormat());
		
		return comments;
	}

	public void removeComment(
			CommentsModeratorRequestParameters requestParameters,
			CommentValueObject comment, String sessionId)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {
		commentsDao.updateCommentStatus(comment.getAnnotationId(), false);
		comment.setStatus(false);
		AnnotationLogValueObject annotationLog = null;
		try {
			annotationLog = AnnotationLogValueObjectHelper
					.createAnnotationLogForInsert(comment, requestParameters,
							sessionId,
							CommonValueObjectConstants.ACTION_REMOVE,
							OPAAuthenticationProvider
									.getAccountIdForLoggedInUser());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		annotationLogDao.disableByAnnotationId(comment.getAnnotationId(), "CM");
		annotationLogDao.insert(annotationLog);
	}

	@Override
	public void restoreComment(
			CommentsModeratorRequestParameters requestParameters,
			CommentValueObject comment, String sessionId)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {
		commentsDao.updateCommentStatus(comment.getAnnotationId(), true);
		comment.setStatus(true);
		AnnotationLogValueObject annotationLog = null;
		try {
			annotationLog = AnnotationLogValueObjectHelper
					.createAnnotationLogForInsert(comment, requestParameters,
							sessionId,
							CommonValueObjectConstants.ACTION_RESTORE,
							OPAAuthenticationProvider
									.getAccountIdForLoggedInUser());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		annotationLogDao.insert(annotationLog);
	}
}
