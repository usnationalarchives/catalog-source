package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.common.accounts.AdministratorRequestParameters;
import gov.nara.opa.api.validation.common.accounts.CommonModifyUserAccountRequestParameters;
import gov.nara.opa.api.validation.constraint.AccountReasonIdExists;
import gov.nara.opa.api.validation.constraint.UserNameExists;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaEmail;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;
import gov.nara.opa.architecture.web.validation.constraint.OpaUserPassword;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

public class AdministratorModifyAccountRequestParameters extends
    AbstractRequestParameters implements UserAccountValueObjectConstants,
    CommonModifyUserAccountRequestParameters, AdministratorRequestParameters {

  @OpaNotNullAndNotEmpty
  @UserNameExists
  private String userName;

  @OpaUserPassword
  private String password;

  @OpaUserPassword
  private String newPassword;

  private String fullName;

  @OpaEmail
  private String email;

  private Boolean displayFullName;

  @OpaNotNullAndNotEmpty
  @AccountReasonIdExists
  private Integer reasonId;

  private String notes;

  @OpaPattern(regexp = "(^standard$)|(^power$)", message = ErrorConstants.INVALID_USER_TYPE)
  private String userType;

  @OpaPattern(regexp = "(^regular$)|(^moderator$)|(^accountAdmin$)|(^accountAdminMod$)", message = ErrorConstants.INVALID_ACCOUNT_RIGHTS)
  private String userRights = "regular";
  
  @OpaPattern(regexp = "(^adminChange$)", message = ErrorConstants.INVALID_ACTION_ADMIN_CHANGE_ONLY)
  private String action;

  @Override
  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
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
    this.fullName = fullName;
  }

  @Override
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public Boolean getDisplayFullName() {
    return displayFullName;
  }

  public void setDisplayFullName(Boolean displayFullName) {
    this.displayFullName = displayFullName;
  }

  @Override
  public Integer getReasonId() {
    return reasonId;
  }

  public void setReasonId(Integer reasonId) {
    this.reasonId = reasonId;
  }

  @Override
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
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
    requestParams.put(REASON_ID_ASP, getReasonId());
    requestParams.put(NOTES_ASP, getNotes());
    return requestParams;
  }

  @Override
  public int getRequestType() {
    return ADMIN_REQUEST;
  }

  @Override
  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  @Override
  public String getUserRights() {
    return userRights;
  }

  public void setUserRights(String userRights) {
    this.userRights = userRights;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

}
