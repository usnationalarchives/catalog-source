package gov.nara.opa.api.validation.administrator;

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
public class AdministratorRegisterAccountRequestParameters extends
    AbstractRequestParameters implements UserAccountValueObjectConstants {

  @OpaNotNullAndNotEmpty
  @OpaSize(min = 3, max = 30, message = ArchitectureErrorMessageConstants.TEXT_FIELD_LENGTH_RANGE)
  @UserNameDoesNotExistAlready
  private String userName;

  @OpaNotNullAndNotEmpty
  @OpaPattern(regexp = "(^standard$)|(^power$)", message = ErrorConstants.INVALID_USER_TYPE)
  private String userType;

  @OpaPattern(regexp = "(^regular$)|(^moderator$)|(^accountAdmin$)|(^accountAdminMod$)", message = ErrorConstants.INVALID_ACCOUNT_RIGHTS)
  private String userRights = "regular";

  @OpaNotNullAndNotEmpty
  @OpaEmail
  @EmailDoesNotExistAlready
  private String email;

  @OpaNotNullAndNotEmpty
  private String fullName;

  private Boolean displayFullName = false;

  @OpaUserPassword
  private String password;
  
  //@OpaPattern(regexp = "(^((https?|ftp|file)://)?[-a-zA-Z0-9]\\.*[-a-zA-Z0-9+&@#/%=~_|])", message = ErrorConstants.INVALID_URL)
  //@OpaPattern(regexp = "^(http|https|ftp)\://[a-zA-Z0-9\-\.]+\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\-\._\?\,\'/\\\+&amp;%\$#\=~@])*[^\.\,\)\(\s]$", message = ErrorConstants.INVALID_URL)
  @OpaPattern(regexp = "^(http|https|ftp)://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~@])*[^\\,\\)\\(\\s]$", message = ErrorConstants.INVALID_URL)
  private String returnUrl;
  
  private String returnText;

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

  public String getReturnUrl() {
    return returnUrl;
  }

  public void setReturnUrl(String returnUrl) {
    this.returnUrl = returnUrl;
  }

  public String getReturnText() {
    return returnText;
  }

  public void setReturnText(String returnText) {
    this.returnText = returnText;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(USER_NAME_ASP, getUserName());
    requestParams.put(USER_TYPE_ASP, getUserType());
    requestParams.put(USER_RIGHTS_ASP, getUserRights());
    requestParams.put(EMAIL_ASP, getEmail());
    requestParams.put(FULL_NAME_ASP, getFullName());
    requestParams.put(DISPLAY_FULL_NAME_ASP, isDisplayFullName());
    if(!StringUtils.isNullOrEmtpy(getReturnUrl())) {
      requestParams.put(RETURN_URL, getReturnUrl());
    }
    if(!StringUtils.isNullOrEmtpy(getReturnText())) {
      requestParams.put(RETURN_TEXT, getReturnText());
    }
    
    return requestParams;
  }

}
