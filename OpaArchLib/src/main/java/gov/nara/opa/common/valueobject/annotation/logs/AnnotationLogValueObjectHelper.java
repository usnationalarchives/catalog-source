package gov.nara.opa.common.valueobject.annotation.logs;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.validation.moderator.CommentsModeratorRequestParameters;
import gov.nara.opa.common.validation.moderator.TagsModeratorRequestParameters;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.sql.Timestamp;
import java.util.Date;

public class AnnotationLogValueObjectHelper implements
		CommonValueObjectConstants {

	public static AnnotationLogValueObject createAnnotationLogForInsert(
			TagValueObject tag, String sessionId, String action) {

		return createAnnotationLogForInsert(tag, tag.getAccountId(), sessionId,
				action, null, null, null);
	}

	public static AnnotationLogValueObject createAnnotationLogForInsert(
			TagValueObject tag,
			TagsModeratorRequestParameters requestParameters, String sessionId,
			String action, Integer accountId) {
		return createAnnotationLogForInsert(tag, accountId, sessionId, action,
				requestParameters.getReasonId(), requestParameters.getNotes(),
				tag.getAccountId());
	}

	private static AnnotationLogValueObject createAnnotationLogForInsert(
			TagValueObject tag, Integer accountId, String sessionId,
			String action, Integer reasonId, String notes,
			Integer affectsAccountId) {
		AnnotationLogValueObject log = new AnnotationLogValueObject();
		log.setAnnotationType(AnnotationConstants.ANNOTATION_TYPE_TAG);
		log.setAnnotationId(tag.getAnnotationId());
		log.setLanguageISO(null);
		log.setAnnotationMD5(tag.getAnnotationMD5());
		log.setStatus(tag.getStatus());
		log.setAccountId(accountId);
		log.setAction(action);
		log.setSessionId(sessionId);
		log.setNaId(tag.getNaId());
		log.setObjectId(tag.getObjectId());
		log.setPageNum(tag.getPageNum());
		log.setLogTS(new Timestamp((new Date()).getTime()));
		log.setReasonId(reasonId);
		log.setNotes(notes);
		log.setAffectsAccountId(affectsAccountId);
		return log;
	}

	public static AnnotationLogValueObject createAnnotationLogForInsert(
			CommentValueObject comment, String sessionId, String action) {

		return createAnnotationLogForInsert(comment, comment.getAccountId(),
				sessionId, action, null, null, null);
	}

	public static AnnotationLogValueObject createAnnotationLogForInsert(
			CommentValueObject comment,
			CommentsModeratorRequestParameters requestParameters,
			String sessionId, String action, Integer accountId) {
		return createAnnotationLogForInsert(comment, accountId, sessionId,
				action, requestParameters.getReasonId(),
				requestParameters.getNotes(), comment.getAccountId());
	}

	private static AnnotationLogValueObject createAnnotationLogForInsert(
			CommentValueObject comment, Integer accountId, String sessionId,
			String action, Integer reasonId, String notes,
			Integer affectsAccountId) {
		AnnotationLogValueObject log = new AnnotationLogValueObject();
		log.setAnnotationType(AnnotationConstants.ANNOTATION_TYPE_COMMENT);
		log.setAnnotationId(comment.getAnnotationId());
		log.setLanguageISO(null);
		log.setParentId(comment.getParentId());
		log.setAnnotationMD5(comment.getAnnotationMD5());
		log.setStatus(comment.getStatus());
		log.setAccountId(accountId);
		log.setAction(action);
		log.setSessionId(sessionId);
		log.setNaId(comment.getNaId());
		log.setObjectId(comment.getObjectId());
		log.setPageNum(comment.getPageNum());
		log.setLogTS(new Timestamp((new Date()).getTime()));
		log.setReasonId(reasonId);
		log.setNotes(notes);
		log.setAffectsAccountId(affectsAccountId);
		return log;
	}
}
