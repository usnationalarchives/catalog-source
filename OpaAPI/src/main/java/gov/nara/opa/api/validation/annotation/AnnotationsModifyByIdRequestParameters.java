package gov.nara.opa.api.validation.annotation;

import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import java.util.LinkedHashMap;

public class AnnotationsModifyByIdRequestParameters extends AnnotationsByIdRequestParameters {

	@OpaNotNullAndNotEmpty
	private String text;

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put(ANNOTATION_ID_ASP, getAnnotationId());
		return requestParams;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
