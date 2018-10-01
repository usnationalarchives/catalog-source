package gov.nara.opa.api.services.impl.user.accounts;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.services.user.accounts.PasswordUserAccountService;
import gov.nara.opa.api.user.accounts.email.UserAccountEmailHelper;
import gov.nara.opa.api.utils.PasswordUtils;
import gov.nara.opa.api.validation.user.accounts.SetNewPasswordRequestParameters;
import gov.nara.opa.api.valueobject.user.accounts.UserAccountValueObjectHelper;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class PasswordUserAccountServiceImpl implements
	PasswordUserAccountService {

	@Autowired
	UserAccountDao userAccountDao;

	@Autowired
	private UserAccountEmailHelper emailHelper;

	@Autowired
	UserAccountValueObjectHelper userAccountValueObjectHelper;

	@Override
	public void requestPasswordReset(UserAccountValueObject userAccount) {
		PasswordUtils.setSecurityInformation(userAccount);
		userAccountDao.update(userAccount);
		emailHelper.sendPasswordReset(userAccount);
	}

	@Override
	public void resetAccountPassword(UserAccountValueObject userAccount) {
		userAccount.setLastActionTS(TimestampUtils.getUtcTimestamp());
		userAccount.setVerificationGuid(null);
		userAccount.setVerificationTS(null);
		userAccountDao.update(userAccount);
		emailHelper.sendPasswordResetFailure(userAccount);

	}

	@Override
	public boolean passwordMatches(UserAccountValueObject userAccount,
			String providedPassword) {
		return PasswordUtils.passwordMatches(userAccount.getpWord(),
				providedPassword);
	}

	@Override
	public void setAccountNewPassword(
			SetNewPasswordRequestParameters requestParameters,
			UserAccountValueObject userAccount) {
		userAccountValueObjectHelper.prepareUserAccountForUpdate(userAccount,
				requestParameters);
		userAccountDao.update(userAccount);
	}

}
