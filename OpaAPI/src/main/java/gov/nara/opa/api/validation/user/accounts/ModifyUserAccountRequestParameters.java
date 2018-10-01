package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.api.validation.common.accounts.CommonModifyUserAccountRequestParameters;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaEmail;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.architecture.web.validation.constraint.OpaUserPassword;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

public class ModifyUserAccountRequestParameters extends
    AbstractRequestParameters implements UserAccountValueObjectConstants,
    CommonModifyUserAccountRequestParameters {

  @OpaNotNullAndNotEmpty
  private String userName;

  @OpaUserPassword
  private String password;

  @OpaUserPassword(message = ArchitectureErrorMessageConstants.INVALID_NEW_PASSWORD)
  private String newPassword;

  @OpaSize(min = 3, max = 30, message = ArchitectureErrorMessageConstants.TEXT_FIELD_LENGTH_RANGE)
  private String fullName;

  Boolean displayFullName;

  @OpaEmail
  String email;

  @Override
  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    if (userName != null) {
      this.userName = userName.trim();
    }
  }

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  @Override
  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    if (fullName != null) {
      this.fullName = fullName.trim();
    }
  }

  @Override
  public Boolean getDisplayFullName() {
    return displayFullName;
  }

  public void setDisplayFullName(Boolean displayFullName) {
    this.displayFullName = displayFullName;
  }

  @Override
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    if (email != null) {
      this.email = email.trim();
    }

  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    if (getFullName() != null) {
      requestParams.put(FULL_NAME_ASP, getFullName());
    }
    if (getDisplayFullName() != null) {
      requestParams.put(DISPLAY_FULL_NAME_ASP, getDisplayFullName());
    }
    if (getEmail() != null) {
      requestParams.put(EMAIL_ASP, getEmail());
    }

    return requestParams;
  }

  @Override
  public int getRequestType() {
    return USER_REQUEST;
  }

  @Override
  public String getUserType() {
    return null;
  }

  @Override
  public String getUserRights() {
    return null;
  }
}
