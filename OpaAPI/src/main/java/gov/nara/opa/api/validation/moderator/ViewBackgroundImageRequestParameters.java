package gov.nara.opa.api.validation.moderator;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;

import java.util.LinkedHashMap;

public class ViewBackgroundImageRequestParameters extends
		AbstractRequestParameters {

	private String naId;

	private String objectId;

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

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		return requestParams;
	}

}
