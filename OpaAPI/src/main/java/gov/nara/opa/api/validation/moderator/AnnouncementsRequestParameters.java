package gov.nara.opa.api.validation.moderator;

import java.util.LinkedHashMap;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;

public class AnnouncementsRequestParameters extends AbstractRequestParameters {

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		return requestParams;
	}
}
