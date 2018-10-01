package gov.nara.opa.api.validation.user.accounts;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaUserPassword;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;

public class SetNewPasswordRequestParameters extends AbstractRequestParameters
    implements UserAccountValueObjectConstants {

  @OpaUserPassword
  public String password;

  public String verificationPassword;

  public String userName;

  public String resetCode;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(USER_NAME_ASP, getUserName());
    requestParams.put(RESET_CODE_ASP, getResetCode());
    return requestParams;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getVerificationPassword() {
    return verificationPassword;
  }

  public void setVerificationPassword(String verificationPassword) {
    this.verificationPassword = verificationPassword;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getResetCode() {
    return resetCode;
  }

  public void setResetCode(String resetCode) {
    this.resetCode = resetCode;
  }

}
