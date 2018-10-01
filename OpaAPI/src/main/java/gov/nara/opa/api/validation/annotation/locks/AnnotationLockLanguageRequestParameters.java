package gov.nara.opa.api.validation.annotation.locks;

import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObjectConstants;

import java.util.LinkedHashMap;

public class AnnotationLockLanguageRequestParameters extends AnnotationLockRequestParameters 
		implements AnnotationLockValueObjectConstants {

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
		return requestParams;
	}
}
