package gov.nara.opa.api.services.administrator;

import gov.nara.opa.api.validation.administrator.AdministratorRegisterAccountRequestParameters;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

/**
 * Service methods for User Account actions performed by administrators
 */
public interface AdministratorRegisterAccountService {
  /**
   * Register/creates a user account
   * 
   * @param request
   *          The Request POJO that contains all parameters (and validated)
   *          passed in from the client
   * @return A POJO representing the user account that was created
   */
  UserAccountValueObject registerAccount(
      AdministratorRegisterAccountRequestParameters request);
}
