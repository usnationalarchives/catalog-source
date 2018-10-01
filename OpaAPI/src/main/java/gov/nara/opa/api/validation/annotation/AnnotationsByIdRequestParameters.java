package gov.nara.opa.api.validation.annotation;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObjectConstants;

import java.util.LinkedHashMap;

public class AnnotationsByIdRequestParameters extends AbstractRequestParameters 
	implements CommentValueObjectConstants {

	@OpaNotNullAndNotEmpty
	private Integer annotationId;

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put(ANNOTATION_ID_ASP, getAnnotationId());
		return requestParams;
	}

	public int getAnnotationId() {
		return annotationId;
	}

	public void setAnnotationId(int annotationId) {
		this.annotationId = annotationId;
	}

}
