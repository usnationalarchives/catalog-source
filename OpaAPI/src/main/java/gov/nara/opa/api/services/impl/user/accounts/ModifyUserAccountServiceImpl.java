package gov.nara.opa.api.services.impl.user.accounts;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.user.logs.AccountLogDao;
import gov.nara.opa.api.services.user.accounts.ModifyUserAccountService;
import gov.nara.opa.api.services.user.lists.DeleteUserListService;
import gov.nara.opa.api.user.accounts.email.UserAccountEmailHelper;
import gov.nara.opa.api.validation.user.accounts.ModifyUserAccountRequestParameters;
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
public class ModifyUserAccountServiceImpl implements ModifyUserAccountService {

	@Autowired
	UserAccountDao userAccountDao;

	@Autowired
	UserAccountValueObjectHelper userAccountValueObjectHelper;

	@Autowired
	AccountLogDao accountLogDao;

	@Autowired
	private DeleteUserListService deleteUserListService;

	@Autowired
	private UserAccountEmailHelper emailHelper;

	@Override
	public void update(UserAccountValueObject userAccount,
			ModifyUserAccountRequestParameters requestParameters) {
		userAccountValueObjectHelper.prepareUserAccountForUpdate(userAccount,
				requestParameters);
		userAccountDao.update(userAccount);
		AccountLogValueObject log = AccountLogValueObjectHelper
				.createAccountLogForInsert(userAccount,
						CommonValueObjectConstants.ACTION_MODIFY);
		accountLogDao.create(log);
		if (requestParameters.getEmail() != null) {
			emailHelper.sendEmailVerification(userAccount);
		}
	}

	@Override
	public void deactivateAccount(UserAccountValueObject userAccount) {
		// Delete user lists
		deleteUserListService.deleteAllUserLists(userAccount.getAccountId());
		userAccount.setAccountStatus(false);
		userAccountDao.update(userAccount);
		emailHelper.sendDeactivationMessage(userAccount);

	}

	@Override
	public void lockAccount(UserAccountValueObject userAccount) {
		userAccount.setAccountStatus(false);
		userAccountDao.update(userAccount);

	}
}
