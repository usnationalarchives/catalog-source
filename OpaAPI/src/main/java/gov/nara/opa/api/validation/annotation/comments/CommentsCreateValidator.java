package gov.nara.opa.api.validation.annotation.comments;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import gov.nara.opa.api.constants.AnnotationsConstants;
import gov.nara.opa.api.system.Config;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.AnnotationsCommonValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.FieldConstraintConstants;

@Component
public class CommentsCreateValidator extends AnnotationsCommonValidator {

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		annotationMaxSizeLength = FieldConstraintConstants.MAX_COMMENT_SIZE_LENGTH;
		ANNOTATION_TEXT = "Comment or reply";
		if (validationResult.isValid()) {
			CommentsCreateRequestParameters commentsParameters = (CommentsCreateRequestParameters) validationResult
					.getValidatedRequest();
			validateAnnotationText(commentsParameters.getText(), validationResult);
			validateIds(commentsParameters, validationResult);
			validatePageNum(commentsParameters, validationResult);
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