package gov.nara.opa.api.validation.annotation;

import java.util.LinkedHashMap;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;

public abstract class AnnotationsViewRequestParameters extends AbstractRequestParameters {

	@OpaNotNullAndNotEmpty
	@OpaSize(max = FieldConstraintConstants.MAX_NAID_SIZE_LENGTH, 
			message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
	private String naId;

	@OpaSize(max = FieldConstraintConstants.MAX_OBJECTID_SIZE_LENGTH, 
			message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
	private String objectId;

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		return null;
	}

	private int pageNum;

	private int annotationId;
	
	private String text;

	public String getNaId() {
		return naId;
	}

	public void setNaId(String naId) {
		this.naId = naId;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getAnnotationId() {
		return annotationId;
	}

	public void setAnnotationId(int annotationId) {
		this.annotationId = annotationId;
	}

}
