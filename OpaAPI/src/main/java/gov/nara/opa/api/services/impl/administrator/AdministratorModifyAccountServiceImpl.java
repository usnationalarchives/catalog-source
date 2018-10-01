package gov.nara.opa.api.services.impl.administrator;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.user.logs.AccountLogDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.administrator.AdministratorModifyAccountService;
import gov.nara.opa.api.user.accounts.email.UserAccountEmailHelper;
import gov.nara.opa.api.utils.PasswordUtils;
import gov.nara.opa.api.validation.administrator.AdministratorDeactivateAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorModifyAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorReactivateAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorRequestPasswordResetRequestParameters;
import gov.nara.opa.api.valueobject.user.accounts.UserAccountValueObjectHelper;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObject;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObjectHelper;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AdministratorModifyAccountServiceImpl implements
    AdministratorModifyAccountService {

  @Autowired
  UserAccountDao userAccountDao;

  @Autowired
  AccountLogDao accountLogDao;

  @Autowired
  UserAccountValueObjectHelper userAccountValueObjecHelper;

  @Autowired
  private UserAccountEmailHelper emailHelper;

  @Override
  public void update(UserAccountValueObject userAccount,
      AdministratorModifyAccountRequestParameters requestParameters) {
    userAccountValueObjecHelper.prepareUserAccountForUpdate(userAccount,
        requestParameters);
    userAccountDao.update(userAccount);
    AccountLogValueObject log = AccountLogValueObjectHelper
        .createAccountLogForInsert(userAccount,
            CommonValueObjectConstants.ACTION_MODIFY,
            OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
            requestParameters.getReasonId(), requestParameters.getNotes());
    accountLogDao.create(log);
  }

  @Override
  public void deactivate(UserAccountValueObject userAccount,
      AdministratorDeactivateAccountRequestParameters requestParameters) {

    userAccountValueObjecHelper.prepareUserAccountForActivationChange(
        userAccount, false);
    userAccountDao.update(userAccount);

    AccountLogValueObject log = AccountLogValueObjectHelper
        .createAccountLogForInsert(userAccount,
            CommonValueObjectConstants.ACTION_DEACTIVATE,
            OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
            requestParameters.getReasonId(), requestParameters.getNotes());
    accountLogDao.create(log);
  }

  @Override
  public void reactivate(UserAccountValueObject userAccount,
      AdministratorReactivateAccountRequestParameters requestParameters) {

    userAccountValueObjecHelper.prepareUserAccountForActivationChange(
        userAccount, true);
    userAccountDao.update(userAccount);

    AccountLogValueObject log = AccountLogValueObjectHelper
        .createAccountLogForInsert(userAccount,
            CommonValueObjectConstants.ACTION_REACTIVATE,
            OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
            requestParameters.getReasonId(), requestParameters.getNotes());
    accountLogDao.create(log);
  }

  @Override
  public void reactivate(UserAccountValueObject userAccount) {

    userAccountValueObjecHelper.prepareUserAccountForActivationChange(
        userAccount, true);
    userAccountDao.update(userAccount);

  }

  @Override
  public void requestPasswordReset(UserAccountValueObject userAccount,
      AdministratorRequestPasswordResetRequestParameters requestParameters) {
    PasswordUtils.setSecurityInformation(userAccount);
    userAccountDao.update(userAccount);
    AccountLogValueObject log = AccountLogValueObjectHelper
        .createAccountLogForInsert(userAccount,
            CommonValueObjectConstants.ACTION_PASSWORD_RESET_REQUEST,
            OPAAuthenticationProvider.getAccountIdForLoggedInUser(), null, null);
    accountLogDao.create(log);
    emailHelper.sendPasswordReset(userAccount);
  }

}
