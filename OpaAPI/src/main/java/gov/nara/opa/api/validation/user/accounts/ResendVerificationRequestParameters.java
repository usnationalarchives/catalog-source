package gov.nara.opa.api.validation.user.accounts;

import java.util.LinkedHashMap;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

public class ResendVerificationRequestParameters extends
    AbstractRequestParameters implements UserAccountValueObjectConstants {

  @OpaNotNullAndNotEmpty
  String userName;
  
  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>();
    
    results.put(USER_NAME_ASP, userName);
    
    return results;
  }

}
