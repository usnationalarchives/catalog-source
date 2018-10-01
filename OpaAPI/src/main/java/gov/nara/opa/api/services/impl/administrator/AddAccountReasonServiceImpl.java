package gov.nara.opa.api.services.impl.administrator;

import gov.nara.opa.api.dataaccess.administrator.AccountReasonDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.administrator.AddAccountReasonService;
import gov.nara.opa.api.validation.administrator.AddAccountReasonRequestParameters;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObjectHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AddAccountReasonServiceImpl implements AddAccountReasonService {

  @Autowired
  AccountReasonDao accountReasonDao;

  @Override
  public AccountReasonValueObject create(
      AddAccountReasonRequestParameters requestParameters) {
    AccountReasonValueObject accountReason = AccountReasonValueObjectHelper
        .createAccountResonForInsert(requestParameters,
            OPAAuthenticationProvider.getAccountIdForLoggedInUser());
    accountReasonDao.create(accountReason);
    return accountReason;
  }
}
