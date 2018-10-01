package gov.nara.opa.api.validation.system;

import java.util.LinkedHashMap;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;

public class LanguageRequestParameters  extends AbstractRequestParameters {

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		return requestParams;
	}

}
