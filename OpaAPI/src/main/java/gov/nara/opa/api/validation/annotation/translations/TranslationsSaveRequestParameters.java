package gov.nara.opa.api.validation.annotation.translations;

import java.util.LinkedHashMap;

import gov.nara.opa.api.validation.annotation.AnnotationsRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;
import gov.nara.opa.common.valueobject.annotation.translations.TranslationValueObjectConstants;

public class TranslationsSaveRequestParameters extends AnnotationsRequestParameters 
	implements TranslationValueObjectConstants {

	@OpaNotNullAndNotEmpty
	@OpaSize(max = FieldConstraintConstants.LANGUAGE_ISO_639_3_SIZE, 
			min = FieldConstraintConstants.LANGUAGE_ISO_639_3_SIZE,
			message = ArchitectureErrorMessageConstants.INVALID_SIZE)
	private String language;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put(NA_ID_ASP, getNaId());
		requestParams.put(OBJECT_ID_ASP, getObjectId());
		requestParams.put(LANGUAGE_ASP, getLanguage());
		requestParams.put(PAGE_NUM_ASP, getPageNum());
		requestParams.put(ANNOTATION_TRANSLATION_ASP, getText());
		requestParams.put(ANNOTATION_ID_ASP, getAnnotationId());
		return requestParams;
	}

}