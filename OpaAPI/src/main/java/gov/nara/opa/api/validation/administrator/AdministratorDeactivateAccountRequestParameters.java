package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.constraint.AccountReasonIdExists;
import gov.nara.opa.api.validation.constraint.UserNameExists;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

public class AdministratorDeactivateAccountRequestParameters extends
    AbstractRequestParameters implements UserAccountValueObjectConstants {

  @OpaNotNullAndNotEmpty
  @AccountReasonIdExists
  private Integer reasonId;

  private String notes;

  @OpaNotNullAndNotEmpty
  @UserNameExists
  private String userName;
  
  @OpaPattern(regexp = "(^adminChange$)", message = ErrorConstants.INVALID_ACTION_ADMIN_CHANGE_ONLY)
  private String action;
  
  @OpaPattern(regexp = "(^active$)|(^inactive$)", message = ErrorConstants.INVALID_STATUS)
  private String status;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(USER_NAME_ASP, getUserName());
    requestParams.put(REASON_ID_ASP, getReasonId());
    requestParams.put(NOTES_ASP, getNotes());
    return requestParams;
  }

  public Integer getReasonId() {
    return reasonId;
  }

  public void setReasonId(Integer reasonId) {
    this.reasonId = reasonId;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
