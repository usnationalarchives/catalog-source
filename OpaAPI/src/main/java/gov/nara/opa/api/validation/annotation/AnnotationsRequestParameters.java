package gov.nara.opa.api.validation.annotation;

import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

public class AnnotationsRequestParameters extends AnnotationsViewRequestParameters {
	
	@OpaNotNullAndNotEmpty
	private String text;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
