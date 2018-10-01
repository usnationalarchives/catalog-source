package gov.nara.opa.api.services.administrator;

import gov.nara.opa.api.validation.administrator.AdministratorDeactivateAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorModifyAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorReactivateAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorRequestPasswordResetRequestParameters;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

public interface AdministratorModifyAccountService {

  void update(UserAccountValueObject userAccount,
      AdministratorModifyAccountRequestParameters requestParameters);

  void deactivate(UserAccountValueObject userAccount,
      AdministratorDeactivateAccountRequestParameters requestParameters);

  void reactivate(UserAccountValueObject userAccount,
      AdministratorReactivateAccountRequestParameters requestParameters);

  void reactivate(UserAccountValueObject userAccount);

  void requestPasswordReset(UserAccountValueObject userAccount,
      AdministratorRequestPasswordResetRequestParameters requestParameters);
}
