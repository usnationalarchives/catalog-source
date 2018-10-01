package gov.nara.opa.api.services.user.accounts;

import gov.nara.opa.api.validation.user.accounts.SetNewPasswordRequestParameters;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

public interface PasswordUserAccountService {

  void requestPasswordReset(UserAccountValueObject userAccount);

  void resetAccountPassword(UserAccountValueObject userAccount);

  void setAccountNewPassword(SetNewPasswordRequestParameters requestParameters,
      UserAccountValueObject userAccount);

  boolean passwordMatches(UserAccountValueObject userAccount,
      String providedPassword);
}
