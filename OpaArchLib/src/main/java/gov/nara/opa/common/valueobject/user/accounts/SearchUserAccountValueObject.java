package gov.nara.opa.common.valueobject.user.accounts;

import gov.nara.opa.architecture.utils.TimestampUtils;

import java.util.LinkedHashMap;

public class SearchUserAccountValueObject extends UserAccountValueObject
    implements UserAccountValueObjectConstants {
  
  private UserAccountValueObject internalObject;
  
  private boolean showPrivateData;

  public SearchUserAccountValueObject(UserAccountValueObject user) {
    internalObject = user;
  }
  
  
  public boolean isShowPrivateData() {
    return showPrivateData;
  }

  public void setShowPrivateData(boolean showPrivateData) {
    this.showPrivateData = showPrivateData;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

    result.put(USER_INTERNAL_ID_ASP, internalObject.getAccountId());
    result.put(USER_ID_ASP, internalObject.getUserName());
    result.put(TYPE_ASP, internalObject.getAccountType());
    result.put(RIGHTS_ASP, internalObject.getAccountRights());

    if(internalObject.getDisplayFullName() || showPrivateData) {
      result.put(FULL_NAME_ASP, internalObject.getFullName());
    }
    
    if(showPrivateData) {
      result.put(EMAIL_ASP, internalObject.getEmailAddress());
    }
    result.put(DISPLAY_FULL_NAME_ASP, internalObject.getDisplayFullName());
    result.put(STATUS_NAME_ASP, (internalObject.getAccountStatus() ? ACTIVE_CODE
        : INACTIVE_CODE));
    result.put(HAS_NOTE_ASP, internalObject.isAccountNoteFlag());
    result.put(IS_NARA_STAFF_ASP, internalObject.isNaraStaff());
    result.put(ACCOUNT_CREATED_TS_ASP,
        TimestampUtils.getUtcString(internalObject.getAccountCreatedTS()));
    return result;
  }
  
}
