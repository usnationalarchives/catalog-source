package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

public class RequestPasswordResetRequestParameters extends
    AbstractRequestParameters implements UserAccountValueObjectConstants {

  @OpaNotNullAndNotEmpty
  String userName;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(USER_NAME_ASP, getUserName());
    return requestParams;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

}
