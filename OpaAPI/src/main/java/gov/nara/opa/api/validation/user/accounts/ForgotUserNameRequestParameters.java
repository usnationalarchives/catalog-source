package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaEmail;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

public class ForgotUserNameRequestParameters extends AbstractRequestParameters
    implements UserAccountValueObjectConstants {

  @OpaEmail
  @OpaNotNullAndNotEmpty
  private String email;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(EMAIL_ASP, getEmail());
    return requestParams;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}
