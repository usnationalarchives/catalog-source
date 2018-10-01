package gov.nara.opa.api.validation.annotation.translations;

import gov.nara.opa.api.constants.AnnotationsConstants;
import gov.nara.opa.api.validation.annotation.AnnotationsCommonValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class TranslationsSaveValidator extends AnnotationsCommonValidator {

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		annotationMaxSizeLength = Integer.MAX_VALUE;
		ANNOTATION_TEXT = "Translation";
		if (validationResult.isValid()) {
			TranslationsSaveRequestParameters translationsParameters =
					(TranslationsSaveRequestParameters) validationResult.getValidatedRequest();
			validateAction(request.getParameter(AnnotationsConstants.ACTION_FIELD_NAME), validationResult);
			validateLanguage(translationsParameters.getLanguage());
			validateAnnotationText(translationsParameters.getText(), validationResult);
			validateIds(translationsParameters, validationResult);
			validatePageNum(translationsParameters, validationResult);
		}
	}

	protected int getAnnotationTextMaxLength() {
		return Integer.MAX_VALUE;
	}

	private boolean validateLanguage(String language) {
		return true;
	}

}
