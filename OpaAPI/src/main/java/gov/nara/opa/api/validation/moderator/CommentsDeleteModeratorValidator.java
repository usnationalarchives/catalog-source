package gov.nara.opa.api.validation.moderator;

import gov.nara.opa.api.dataaccess.moderator.AnnotationReasonDao;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.valueobject.moderator.AnnotationReasonValueObject;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.NumericConstants;
import gov.nara.opa.common.dataaccess.annotation.comments.CommentsDao;
import gov.nara.opa.common.validation.moderator.CommentsModeratorRequestParameters;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;

import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CommentsDeleteModeratorValidator extends OpaApiAbstractValidator {

	@Autowired
	CommentsDao commentDao;

	@Autowired
	AnnotationReasonDao annotationReasonDao;

	public static final String COMMENT_VALUE_OBJECT_KEY = "comment";

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		CommentsModeratorRequestParameters requestParameters = (CommentsModeratorRequestParameters) validationResult
				.getValidatedRequest();

		validateLogNotesLength(validationResult, requestParameters.getNotes().length());		
		if (!validationResult.isValid()) {
			return;
		}
		
		CommentValueObject foundComment = commentDao
				.getCommentByAnnotationId(requestParameters.getAnnotationId());

		validateCommentExists(foundComment, validationResult);
		
		if (!validationResult.isValid()) {
			return;
		}

		validationResult.addContextObject(COMMENT_VALUE_OBJECT_KEY,
				foundComment);
		validateReasonId(foundComment, validationResult,
				requestParameters.getReasonId());
	}

	private void validateLogNotesLength(ValidationResult validationResult, int notesLength) {

		if (notesLength > NumericConstants.NOTES_LENGTH) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
			error.setErrorMessage(String.format(
					ErrorConstants.INVALID_NOTES_SIZE, notesLength, NumericConstants.NOTES_LENGTH));
			validationResult.addCustomValidationError(error);
		}
	}
	
	private void validateCommentExists(CommentValueObject comment,
			ValidationResult validationResult) {
		if (comment == null) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.COMMENT_NOT_FOUND);
			error.setErrorMessage(ErrorConstants.ACTIVE_COMMENT_NOT_FOUND);
			validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
			validationResult.addCustomValidationError(error);
		}
	}

	private void validateReasonId(CommentValueObject comment,
			ValidationResult validationResult, Integer reasonId) {

		AnnotationReasonValueObject annotationReason = annotationReasonDao
				.getAnnotationReasonById(reasonId);

		if (annotationReason == null || !annotationReason.getStatus()) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
			error.setErrorMessage(String.format(
					ErrorConstants.REASON_ID_DOES_NOT_EXIST, reasonId));
			validationResult.addCustomValidationError(error);
		}
	}

	@Override
	protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
		// TODO Auto-generated method stub
		return null;
	}

}
