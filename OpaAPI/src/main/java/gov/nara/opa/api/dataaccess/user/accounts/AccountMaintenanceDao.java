package gov.nara.opa.api.dataaccess.user.accounts;

import gov.nara.opa.api.valueobject.administrator.AccountMaintenanceValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface AccountMaintenanceDao {

	public List<AccountMaintenanceValueObject> getIdleAccounts(int daysForDeactivation, 
			int daysForFirstWarning, int daysForSecondWarning) 
					throws DataAccessException, UnsupportedEncodingException, BadSqlGrammarException;

	public List<AccountMaintenanceValueObject> getUnverifiedAccounts(int registerVerificationMaximumDays)
			throws DataAccessException, UnsupportedEncodingException, BadSqlGrammarException;

	public List<AccountMaintenanceValueObject> getUnverifiedEmailChanges(int emailChangeVerificationMaximumDays)
			throws DataAccessException, UnsupportedEncodingException, BadSqlGrammarException;
	
}
