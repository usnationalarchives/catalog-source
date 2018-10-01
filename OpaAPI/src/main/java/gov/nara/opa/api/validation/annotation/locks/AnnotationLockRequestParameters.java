package gov.nara.opa.api.validation.annotation.locks;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObjectConstants;

import java.util.LinkedHashMap;

public class AnnotationLockRequestParameters extends AbstractRequestParameters
	implements AnnotationLockValueObjectConstants {

	@OpaNotNullAndNotEmpty
	@OpaSize(max = FieldConstraintConstants.MAX_NAID_SIZE_LENGTH, 
			message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
	private String naId;

	@OpaNotNullAndNotEmpty
	@OpaSize(max = FieldConstraintConstants.MAX_OBJECTID_SIZE_LENGTH, 
			message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
	private String objectId;

	@OpaNotNullAndNotEmpty
	private String action;

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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		return requestParams;
	}
}
