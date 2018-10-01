package gov.nara.opa.api.usagelogging.annotation.comments;

import gov.nara.opa.api.usagelogging.annotation.tags.TagsLogger;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentsCollectionValueObject;

public class CommentsLogger {

	public static final String DELETE_COMMENT_ACTION = "deleteComment";

	static OpaLogger log = OpaLogger.getLogger(TagsLogger.class);

	public static final String COMMENTS_NO_OF_USER_CONTRIBUTIONS_VIA_API_MESSGE = "Action=%1$s, AnnotationType=%2$s, "
			+ "Naid=%3$s, Object=%4$s";
	public static final String INFO_COMMENT_MESSAGE = "naId=%1$s,objectId=%2$s,action=%3$s,commentText=%4$s";

	public static void logComments(CommentsCollectionValueObject comments,
			Class<? extends AbstractBaseController> controller, String action,
			String apiType) {

		for (CommentValueObject comment : comments.getComments()) {
			logComment(comment, controller, action, apiType);
		}
	}

	public static void logComment(CommentValueObject comment,
			Class<?> controller, String action, String apiType) {
		String naId = comment.getNaId();
		String objectId = comment.getObjectId() == null ? "" : comment
				.getObjectId();

		if (!action.equals(DELETE_COMMENT_ACTION)) {
			log.usage(
					controller,
					ApiTypeLoggingEnum.toApiTypeLoggingEnum(apiType),
					UsageLogCode.COMMENT,
					String.format(
							COMMENTS_NO_OF_USER_CONTRIBUTIONS_VIA_API_MESSGE,
							action,
							AnnotationConstants.ANNOTATION_TYPE_COMMENT_LOGS,
							naId, objectId));
		}

		log.info(String.format(INFO_COMMENT_MESSAGE, naId, objectId, action,
				comment.getAnnotation()));
	}

}
