package gov.nara.opa.api.validation.annotation.comments;


import java.io.File;

import gov.nara.opa.api.constants.AnnotationsConstants;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.system.Config;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.AnnotationsCommonValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.annotation.comments.CommentsDao;
import gov.nara.opa.common.validation.FieldConstraintConstants;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CommentsModifyValidator extends AnnotationsCommonValidator {

	@Autowired
	CommentsDao commentsDao;

	public static final String COMMENTS_TEXT_FIELD_NAME = "text";
	public static final String COMMENT_VALUE_OBJECT_KEY = "comment";

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		annotationMaxSizeLength = FieldConstraintConstants.MAX_COMMENT_SIZE_LENGTH;
		CommentsAndRepliesRequestParameters requestParameters = (CommentsAndRepliesRequestParameters) validationResult
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
		validateAnnotationText(requestParameters.getText(), validationResult);
		if (!validationResult.isValid()) {
			return;
		}

		if (!(validateAnnotationContent(requestParameters.getText(),
				validationResult))) {
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

	protected int getAnnotationTextMaxLength(ValidationResult validationResult) {
		Config config = configService.getConfig(configFilePath);

		// If the config.xml file returns successfully
		File file = new File(configFilePath);

		// If the config.xml file returns successfully
		if (config == null || !file.exists()) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.CONFIG_FILE_NOT_FOUND,
					ErrorConstants.CONFIG_FILE_NOT_FOUND, AnnotationsConstants.TEXT_FIELD_NAME);
			return -1;
		}
		return config.getCommentsLength();
	}
}
