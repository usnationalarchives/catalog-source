package gov.nara.opa.api.validation.annotation.comments;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.annotation.AnnotationsByIdRequestParameters;
import gov.nara.opa.api.validation.annotation.AnnotationsCommonValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.annotation.comments.CommentsDao;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CommentsDeleteByIdValidator extends AnnotationsCommonValidator  {

	@Autowired
	CommentsDao commentsDao;

	public static final String ANNTATIONS_TEXT_FIELD_NAME = "text";
	public static final String COMMENT_VALUE_OBJECT_KEY = "comment";

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		AnnotationsByIdRequestParameters requestParameters = 
				(AnnotationsByIdRequestParameters) validationResult
				.getValidatedRequest();
		CommentValueObject foundComment = commentsDao.
				getCommentByAnnotationId(requestParameters.getAnnotationId());

		validateCommentExists(foundComment, validationResult);
		if (!validationResult.isValid()) {
			return;
		}

		validationResult.addContextObject(COMMENT_VALUE_OBJECT_KEY,
				foundComment);
		validateOwner(foundComment, validationResult);
	}

	private void validateCommentExists(CommentValueObject comment,
			ValidationResult validationResult) {
		if (comment == null) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.COMMENT_NOT_FOUND);
			error.setErrorMessage(ErrorConstants.COMMENT_NOT_FOUND);
			validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
			validationResult.addCustomValidationError(error);
		}
	}

	private void validateOwner(CommentValueObject comment,
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
