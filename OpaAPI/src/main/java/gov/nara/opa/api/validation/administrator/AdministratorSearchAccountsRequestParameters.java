package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.architecture.web.validation.AbstractSearchResquestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdministratorSearchAccountsRequestParameters extends
    AbstractSearchResquestParameters implements UserAccountValueObjectConstants {

  private static final int USER_SEARCH_DEFAULT_NUMBER_OF_ROWS = 10;
  private static final String USER_SEARCH_DEFAULT_ACTION = "search";
  
  private Integer internalId;

  private String id;

  private String userType;

  private String email;

  private String userRights;

  private String fullName;

  private Boolean displayFullName;

  private Boolean hasNotes;
  
  @OpaPattern(regexp = "(^search$)", message = ErrorConstants.INVALID_ACTION_SEARCH_ONLY)
  private String action = USER_SEARCH_DEFAULT_ACTION;

  @OpaPattern(regexp = "(^" + CommonValueObjectConstants.ACTIVE_CODE + "$)|(^"
      + CommonValueObjectConstants.INACTIVE_CODE + "$)", message = ErrorConstants.INVALID_STATUS)
  private String status = null;

  
  public AdministratorSearchAccountsRequestParameters() {
    setRows(USER_SEARCH_DEFAULT_NUMBER_OF_ROWS);
  }
  
  private static final Map<String, String> paramNamesToDbColumnsMap = new ConcurrentHashMap<String, String>();
  static {
    paramNamesToDbColumnsMap.put("id", USER_NAME_DB);
    paramNamesToDbColumnsMap.put("email", EMAIL_ADDRESS_DB);
    paramNamesToDbColumnsMap.put("userType", ACCOUNT_TYPE_DB);
    paramNamesToDbColumnsMap.put("userRights", ACCOUNT_RIGHTS_DB);
    paramNamesToDbColumnsMap.put("hasNotes", ACCOUNT_NOTE_FLAG_DB);
    paramNamesToDbColumnsMap.put("fullName", FULL_NAME_DB);
    paramNamesToDbColumnsMap.put("displayFullName", DISPLAY_NAME_FLAG_DB);
    paramNamesToDbColumnsMap.put("internalId", ACCOUNT_ID_DB);
    paramNamesToDbColumnsMap.put("status", ACCOUNT_STATUS_DB);
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();

    UserAccount user = SessionUtils.getSessionUser();
    
    addParameterToMapIfNotNull(USER_NAME_ASP, getId());
    addParameterToMapIfNotNull(USER_TYPE_ASP, getUserType());
    addParameterToMapIfNotNull(USER_RIGHTS_ASP, getUserRights());
    
    if(user != null && (user.isAdministrator() || user.isAdminModerator()) ) {
      addParameterToMapIfNotNull(EMAIL_ASP, getEmail());
      addParameterToMapIfNotNull(FULL_NAME_ASP, getFullName());
    }
    
    addParameterToMapIfNotNull(DISPLAY_FULL_NAME_ASP, getDisplayFullName());
    addParameterToMapIfNotNull(HAS_NOTE_ASP, getHasNotes());

    addParameterToMapIfNotNull(SORT_ASP, getSort());
    addParameterToMapIfNotNull(OFFSET_ASP, getOffset());
    addParameterToMapIfNotNull(ROWS_ASP, getRows());

    return requestParams;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public Boolean getDisplayFullName() {
    return displayFullName;
  }

  public void setDisplayFullName(Boolean displayFullName) {
    this.displayFullName = displayFullName;
  }

  public Boolean getHasNotes() {
    return hasNotes;
  }

  public void setHasNotes(Boolean hasNotes) {
    this.hasNotes = hasNotes;
  }

  public String getUserRights() {
    return userRights;
  }

  public void setUserRights(String userRights) {
    this.userRights = userRights;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getInternalId() {
    return internalId;
  }

  public void setInternalId(Integer internalId) {
    this.internalId = internalId;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Boolean getUserStatus() {
    if (status == null || status.equals(CommonValueObjectConstants.ACTIVE_CODE)) {
      return true;
    }
    return false;
  }

  @Override
  public Map<String, String> getParamNamesToDbColumnsMap() {
    return paramNamesToDbColumnsMap;
  }
}
