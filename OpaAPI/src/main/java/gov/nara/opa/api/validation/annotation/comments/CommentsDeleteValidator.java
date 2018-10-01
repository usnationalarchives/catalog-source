package gov.nara.opa.api.validation.annotation.comments;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.annotation.comments.CommentsDao;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CommentsDeleteValidator extends OpaApiAbstractValidator {

	@Autowired
	CommentsDao commentsDao;

	public static final String COMMENT_VALUE_OBJECT_KEY = "comment";

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		CommentsAndRepliesViewRequestParameters requestParameters = (CommentsAndRepliesViewRequestParameters) validationResult
				.getValidatedRequest();
		CommentValueObject foundComment = null;
		if (requestParameters.getReplyId() == null) {
			foundComment = commentsDao.selectCommentAndReplies(
					requestParameters.getAnnotationId(),
					requestParameters.getNaId(),
					requestParameters.getObjectId());
		} else {
			foundComment = commentsDao.selectCommentAndReplies(
					requestParameters.getReplyId(),
					requestParameters.getNaId(),
					requestParameters.getObjectId());
		}

		validateCommentExists(foundComment, validationResult);
		if (!validationResult.isValid()) {
			return;
		}

		validationResult.addContextObject(COMMENT_VALUE_OBJECT_KEY,
				foundComment);
		validateOwner(foundComment, validationResult);
	}

	protected void validateCommentExists(CommentValueObject comment,
			ValidationResult validationResult) {
		if (comment == null) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.COMMENT_NOT_FOUND);
			error.setErrorMessage(ErrorConstants.COMMENT_NOT_FOUND);
			validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
			validationResult.addCustomValidationError(error);
		}
	}

	protected void validateOwner(CommentValueObject comment,
			ValidationResult validationResult) {
		Integer currentUserId = OPAAuthenticationProvider
				.getAccountIdForLoggedInUser();
		if (!currentUserId.equals(comment.getAccountId())) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.NOT_OWNER);
			error.setErrorMessage(ErrorConstants.COMMENT_NOT_OWNER);
			validationResult.addCustomValidationError(error);
		}
	}
}
