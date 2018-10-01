package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

public class VerifyAccountRequestParameters extends AbstractRequestParameters
	implements UserAccountValueObjectConstants {

	@OpaNotNullAndNotEmpty
	private String activationCode;

	private Boolean showPwdSet = false;

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put(ACTIVATION_CODE_ASP, getActivationCode());
		requestParams.put(SHOW_PWD_SET, getShowPwdSet());
		return requestParams;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public Boolean getShowPwdSet() {
		return showPwdSet;
	}

	public void setShowPwdSet(Boolean showPwdSet) {
		this.showPwdSet = showPwdSet;
	}

}
