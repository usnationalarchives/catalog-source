package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.constraint.EmailDoesNotExistAlready;
import gov.nara.opa.api.validation.constraint.UserNameDoesNotExistAlready;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaEmail;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.architecture.web.validation.constraint.OpaUserPassword;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

/**
 * Request POJO that contains property into which http request
 * parameters are bound by the Spring framework
 * 
 */
public class RegisterAccountRequestParameters extends AbstractRequestParameters
	implements UserAccountValueObjectConstants {

	@OpaNotNullAndNotEmpty
	@OpaSize(min = 3, max = 30, message = ArchitectureErrorMessageConstants.TEXT_FIELD_LENGTH_RANGE)
	@UserNameDoesNotExistAlready
	private String userName;

	@OpaPattern(regexp = "(^standard$)|(^power$)", message = ErrorConstants.INVALID_USER_TYPE)
	private String userType = "standard";

	@OpaPattern(regexp = "(^regular$)|(^moderator$)|(^accountAdmin$)|(^accountAdminMod$)", message = ErrorConstants.INVALID_ACCOUNT_RIGHTS)
	private String userRights = "regular";

	@OpaNotNullAndNotEmpty
	@OpaEmail
	@EmailDoesNotExistAlready
	private String email;

	//@OpaNotNullAndNotEmpty - removed by request NARA-1630
	@OpaSize(min = 3, max = 100, message = ArchitectureErrorMessageConstants.TEXT_FIELD_LENGTH_RANGE)
	private String fullName;

	private Boolean displayFullName = false;

	@OpaNotNullAndNotEmpty
	@OpaUserPassword
	private String password;
	
	private String referringUrl;

	public Boolean isDisplayFullName() {
		return displayFullName;
	}

	public void setDisplayFullName(Boolean displayFullName) {
		this.displayFullName = displayFullName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		if (userName != null) {
			this.userName = userName.trim();
		}
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getUserRights() {
		return userRights;
	}

	public void setUserRights(String userRights) {
		this.userRights = userRights;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email != null) {
			this.email = email.trim();
		}
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		if (fullName != null) {
			this.fullName = fullName.trim();
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getReferringUrl() {
		return referringUrl;
	}

	public void setReferringUrl(String referringUrl) {
		this.referringUrl = referringUrl;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put(USER_NAME_ASP, getUserName());
		requestParams.put(USER_TYPE_ASP, (getUserType() != null ? getUserType()
				.toLowerCase() : ""));
		requestParams.put(USER_RIGHTS_ASP,
				(getUserRights() != null ? getUserRights().toLowerCase() : ""));
		requestParams.put(EMAIL_ASP, (getEmail() != null ? getEmail().toLowerCase()
				: ""));
		requestParams.put(FULL_NAME_ASP, getFullName());
		requestParams.put(DISPLAY_FULL_NAME_ASP, isDisplayFullName());
		
		if(!StringUtils.isNullOrEmtpy(referringUrl)) {
			requestParams.put(REFERRING_URL_ASP, getReferringUrl());
		}
		
		return requestParams;
	}
}
