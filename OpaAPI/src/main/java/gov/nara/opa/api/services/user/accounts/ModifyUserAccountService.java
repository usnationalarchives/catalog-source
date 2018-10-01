package gov.nara.opa.api.services.user.accounts;

import gov.nara.opa.api.validation.user.accounts.ModifyUserAccountRequestParameters;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

public interface ModifyUserAccountService {

  void deactivateAccount(UserAccountValueObject userAccount);

  void lockAccount(UserAccountValueObject userAccount);

  void update(UserAccountValueObject userAccount,
      ModifyUserAccountRequestParameters requestParameters);
}
