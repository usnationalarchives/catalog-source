package gov.nara.opa.api.validation.moderator;

import java.util.LinkedHashMap;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;

public class OnlineAvailabilityHeaderModeratorRequestParameters extends
		AbstractRequestParameters {

	@OpaNotNullAndNotEmpty
	private Integer reasonId;

	private String notes;

	@OpaNotNullAndNotEmpty
	@OpaSize(max = FieldConstraintConstants.MAX_NAID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
	private String naId;

	public String getNaId() {
		return naId;
	}

	public void setNaId(String naId) {
		this.naId = naId;
	}

	public Integer getReasonId() {
		return reasonId;
	}

	public void setReasonId(Integer reasonId) {
		this.reasonId = reasonId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put("naId", naId);
		if (reasonId != null) {
			requestParams.put("reasonId", reasonId);
		}

		if (notes != null && !notes.isEmpty()) {
			requestParams.put("notes", notes);
		} else {
			requestParams.put("notes", "");
		}
		return requestParams;
	}
}
