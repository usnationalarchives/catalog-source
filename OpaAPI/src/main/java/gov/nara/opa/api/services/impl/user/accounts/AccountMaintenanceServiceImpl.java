package gov.nara.opa.api.services.impl.user.accounts;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.user.accounts.AccountMaintenanceDao;
import gov.nara.opa.api.services.export.AccountExportService;
import gov.nara.opa.api.services.user.accounts.AccountMaintenanceService;
import gov.nara.opa.api.services.user.accounts.ModifyUserAccountService;
import gov.nara.opa.api.user.accounts.email.UserAccountEmailHelper;
import gov.nara.opa.api.valueobject.administrator.AccountMaintenanceValueObject;
import gov.nara.opa.api.valueobject.administrator.AccountMaintenanceValueObjectConstants;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

@Component
@Transactional
public class AccountMaintenanceServiceImpl implements
	AccountMaintenanceService, AccountMaintenanceValueObjectConstants {

	private static OpaLogger logger = OpaLogger.getLogger(AccountMaintenanceServiceImpl.class);

	@Value("${daysForDeactivation}")
	private String daysForDeactivation;

	@Value("${daysForFirstWarning}")
	private String daysForFirstWarning;

	@Value("${daysForSecondWarning}")
	private String daysForSecondWarning;

	@Value("${registerVerificationMaximumDays}")
	private String registerVerificationMaximumDays;

	@Value("${emailChangeVerificationMaximumDays}")
	private String emailChangeVerificationMaximumDays;
	
	@Value("${export.output.location}")
	protected String exportOutputLocation;

	@Autowired
	private AccountMaintenanceDao accountMaintenanceDao;

	@Autowired
	private AccountExportService accountExportService;	

	@Autowired
	private ModifyUserAccountService userAccountModifier;

	@Autowired
	private UserAccountDao userAccountDao;

	@Autowired
	private UserAccountEmailHelper emailHelper;

	/*
	 * (non-Javadoc)
	 * @see gov.nara.opa.api.services.user.accounts.AccountMaintenanceService#removeIdleAccounts()
	 */
	@Override
	public int disableIdleAccounts() throws Exception {
		try {
			//Get eligible accounts for deactivation
			List<AccountMaintenanceValueObject> eligibleAccounts = accountMaintenanceDao.
					getIdleAccounts(Integer.parseInt(daysForDeactivation), 
							Integer.parseInt(daysForFirstWarning), Integer.parseInt(daysForSecondWarning));
			//Process accounts
			for(AccountMaintenanceValueObject accountMap : eligibleAccounts) {
				int remainingDays = accountMap.getRemaining();
				int accountId = accountMap.getAccountId();
				String email = accountMap.getEmail();
				String actionType = accountMap.getActionType();
				if(actionType.equals(DEACTIVATE)) {
					//Deactivate account
					deactivateAccount(accountId);
					logger.info(String.format("Disabling account Id: %1$d", accountId));
				} else {
					//Update warning field
					UserAccountValueObject user = userAccountDao.selectByAccountId(accountId);
					if(actionType.equals(SECOND_WARNING)) {
						user.setDeactivationWarning(2);
						logger.info(String.format("Second warning for account Id: %1$d", accountId));
					} else {
						user.setDeactivationWarning(1);
						logger.info(String.format("First warning for account Id: %1$d", accountId));
					}
					//Save user
					userAccountDao.updateDeactivationWarning(user);
					//Send warning email
					sendWarningEmail(email, remainingDays);
				}
			}
			return eligibleAccounts.size();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private void sendWarningEmail(String email, int remainingDays) {
		emailHelper.sendDeactivationWarning(email, remainingDays);
	}

	private void deactivateAccount(int accountId) {
		UserAccountValueObject userAccount = userAccountDao.selectByAccountId(accountId);
		userAccountModifier.deactivateAccount(userAccount);
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nara.opa.api.services.user.accounts.AccountMaintenanceService#removeUnverifiedAccounts()
	 */
	@Override
	public int removeUnverifiedAccounts() throws Exception {
		try {
			//Get eligible accounts for removal
			logger.debug(String.format("Getting unverified accounts with param: %1$s", registerVerificationMaximumDays));
			List<AccountMaintenanceValueObject> accountsToRemove = accountMaintenanceDao.
					getUnverifiedAccounts(Integer.parseInt(registerVerificationMaximumDays));
			logger.debug(String.format("Stored procedure returned accounts: %1$d", accountsToRemove.size()));
			for(AccountMaintenanceValueObject accountMap : accountsToRemove ) {
				logger.debug(String.format("Removing unverified account: %1$d", accountMap.getAccountId()));
				userAccountDao.delete(accountMap.getAccountId());
			}
			return accountsToRemove.size();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nara.opa.api.services.user.accounts.AccountMaintenanceService#removeUnverifiedEmailChanges()
	 */
	@Override
	public int removeUnverifiedEmailChanges() throws Exception {
		try {
			//Get eligible email changes to cancel
			logger.debug(String.format("Getting unverified emails with param: %1$s", emailChangeVerificationMaximumDays));
			List<AccountMaintenanceValueObject> changesToRevert = accountMaintenanceDao.
					getUnverifiedEmailChanges(Integer.parseInt(emailChangeVerificationMaximumDays));
			logger.debug(String.format("Stored procedure returned accounts: %1$d", changesToRevert.size()));
			for (AccountMaintenanceValueObject change : changesToRevert) {
				UserAccountValueObject user = userAccountDao.selectByAccountId(change.getAccountId());
				user.setAdditionalEmailAddress(null);
				user.setAdditionalEmailAddressTs(null);
				userAccountDao.update(user);
			}
			return changesToRevert.size();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public int removeExpiredExports() {
		int returnValue = 0;
		
		try {
			//Get expired exports
			returnValue = accountExportService.removeExpiredExports();
			
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		
		
		return returnValue;
	}
}