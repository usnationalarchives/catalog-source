package gov.nara.opa.api.services.user.accounts;

import gov.nara.opa.api.validation.user.accounts.RegisterAccountRequestParameters;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

public interface RegisterUserAccountService {

	UserAccountValueObject registerAccount(
			RegisterAccountRequestParameters requestParameters);

	void resendVerification(UserAccountValueObject userAccount);

	void verifyAccount(UserAccountValueObject userAccount);

	void verifyAccount(UserAccountValueObject userAccount, boolean showPwdSet);

	void forgotUserName(UserAccountValueObject userAccount);

	void sendEmailVerification(UserAccountValueObject userAccount);

	UserAccountValueObject verifyEmailChange(UserAccountValueObject userAccount);
}
