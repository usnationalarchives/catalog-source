package gov.nara.opa.api.services.impl.administrator;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.administrator.AdministratorRegisterAccountService;
import gov.nara.opa.api.user.accounts.email.UserAccountEmailHelper;
import gov.nara.opa.api.validation.administrator.AdministratorRegisterAccountRequestParameters;
import gov.nara.opa.api.valueobject.user.accounts.UserAccountValueObjectHelper;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObject;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObjectHelper;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * See the implemented interface RegisterAccountService for javadocs
 */
@Component
@Transactional
public class AdministratorRegisterAccountServiceImpl implements
    AdministratorRegisterAccountService {

  @Autowired
  private UserAccountDao userAccountDao;

  @Autowired
  private UserAccountValueObjectHelper userAccountHelper;

  @Autowired
  UserAccountEmailHelper emailHelper;

  @Override
  public UserAccountValueObject registerAccount(
      AdministratorRegisterAccountRequestParameters accountRequest) {
    UserAccountValueObject userAccount = userAccountHelper
        .createUserAccountForInsert(accountRequest);
    userAccountDao.create(userAccount);
    emailHelper.sendRegisterVerification(userAccount, true, accountRequest.getReturnUrl(), accountRequest.getReturnText());
    AccountLogValueObject log = AccountLogValueObjectHelper
        .createAccountLogForInsert(userAccount,
            CommonValueObjectConstants.ACTION_ADD,
            OPAAuthenticationProvider.getAccountIdForLoggedInUser());
    return userAccount;
  }
}
