package gov.nara.opa.api.dataaccess.impl.user.accounts;

import gov.nara.opa.api.dataaccess.user.accounts.AccountMaintenanceDao;
import gov.nara.opa.api.valueobject.administrator.AccountMaintenanceValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AutoDisableUserAccountJDBCTemplate 
	extends AbstractOpaDbJDBCTemplate implements AccountMaintenanceDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<AccountMaintenanceValueObject> getIdleAccounts(
			int daysForDeactivation, int daysForFirstWarning, int daysForSecondWarning)
					throws DataAccessException, UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("daysForDeactivation", daysForDeactivation);
		inParamMap.put("daysForSecondWarning", daysForSecondWarning);
		inParamMap.put("daysForFirstWarning", daysForFirstWarning);
		return (List<AccountMaintenanceValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spSelectIdleAccounts",
						new GenericRowMapper<AccountMaintenanceValueObject>(
								new AutoDisableUserAccountExtractor()), inParamMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AccountMaintenanceValueObject> getUnverifiedAccounts(
			int registerVerificationMaximumDays) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("registerVerificationMaximumDays", registerVerificationMaximumDays);
		return (List<AccountMaintenanceValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spSelectUnverifiedAccounts",
						new GenericRowMapper<AccountMaintenanceValueObject>(
								new AutoDisableUserAccountExtractor()), inParamMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AccountMaintenanceValueObject> getUnverifiedEmailChanges(
			int emailChangeVerificationMaximumDays) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("emailChangeVerificationMaximumDays", emailChangeVerificationMaximumDays);
		return (List<AccountMaintenanceValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spSelectUnverifiedEmailChanges",
						new GenericRowMapper<AccountMaintenanceValueObject>(
								new AutoDisableUserAccountExtractor()), inParamMap);
	}

}
