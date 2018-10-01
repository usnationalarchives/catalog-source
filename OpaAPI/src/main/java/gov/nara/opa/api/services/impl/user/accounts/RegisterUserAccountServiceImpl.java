package gov.nara.opa.api.services.impl.user.accounts;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.user.logs.AccountLogDao;
import gov.nara.opa.api.services.user.accounts.RegisterUserAccountService;
import gov.nara.opa.api.user.accounts.email.UserAccountEmailHelper;
import gov.nara.opa.api.utils.PasswordUtils;
import gov.nara.opa.api.validation.user.accounts.RegisterAccountRequestParameters;
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
public class RegisterUserAccountServiceImpl implements
RegisterUserAccountService {

	@Autowired
	private UserAccountDao administratorUserAccount;

	@Autowired
	private UserAccountValueObjectHelper userAccountHelper;

	@Autowired
	private UserAccountEmailHelper emailHelper;

	@Autowired
	AccountLogDao accountLogDao;

	@Override
	public UserAccountValueObject registerAccount(
			RegisterAccountRequestParameters requestParameters) {
		UserAccountValueObject userAccount = userAccountHelper
				.createUserAccountForInsert(requestParameters);
		administratorUserAccount.create(userAccount);
		AccountLogValueObject log = AccountLogValueObjectHelper
				.createAccountLogForInsert(userAccount,
						CommonValueObjectConstants.ACTION_ADD);
		accountLogDao.create(log);
		emailHelper.sendRegisterVerification(userAccount);
		return userAccount;
	}

	@Override
	public void resendVerification(UserAccountValueObject userAccount) {
		// Update verification timestamp
		PasswordUtils.updateVerificationTimestamp(userAccount);

		// Save changes
		administratorUserAccount.update(userAccount);

		// Resend email
		emailHelper.sendRegisterVerification(userAccount);
	}

	@Override
	public void verifyAccount(UserAccountValueObject userAccount) {
		verifyAccount(userAccount, false);
	}

	public void verifyAccount(UserAccountValueObject userAccount,
			boolean showPwdSet) {
		userAccountHelper.updateUserAccountAfterActivation(userAccount);
		if (showPwdSet) {
			// Set reset password verification
			PasswordUtils.updateVerificationGuid(userAccount);
			PasswordUtils.updateVerificationTimestamp(userAccount);
		}

		administratorUserAccount.update(userAccount);
	}

	@Override
	public void forgotUserName(UserAccountValueObject userAccount) {
		emailHelper.sendUserNameRecovery(userAccount);
	}

	@Override
	public void sendEmailVerification(UserAccountValueObject userAccount) {
		emailHelper.sendEmailVerification(userAccount);
	}

	@Override
	public UserAccountValueObject verifyEmailChange(UserAccountValueObject userAccount) {
		userAccount = administratorUserAccount.selectByUserName(userAccount.getUserName());
		userAccountHelper.updateUserAccountAfterEmailChange(userAccount);
		administratorUserAccount.update(userAccount);
		userAccount = administratorUserAccount.selectByUserName(userAccount.getUserName());
		return userAccount;
	}

}