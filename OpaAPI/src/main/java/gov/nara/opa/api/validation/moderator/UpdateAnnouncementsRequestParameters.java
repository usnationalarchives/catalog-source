package gov.nara.opa.api.validation.moderator;

import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;

public class UpdateAnnouncementsRequestParameters extends
		AnnouncementsRequestParameters {

	@OpaNotNullAndNotEmpty
	@OpaSize(max = FieldConstraintConstants.MAX_ANNOUNCEMENT_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
	private String text;

	private Boolean enabled = false;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}
