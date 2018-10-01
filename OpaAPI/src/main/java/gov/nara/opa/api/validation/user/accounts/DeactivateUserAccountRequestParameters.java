package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaUserPassword;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

public class DeactivateUserAccountRequestParameters extends
    AbstractRequestParameters implements UserAccountValueObjectConstants {

  @OpaNotNullAndNotEmpty
  private String userName;

  @OpaUserPassword
  private String password;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(USER_NAME_ASP, getUserName());
    return requestParams;
  }

}
