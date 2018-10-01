package gov.nara.opa.api.dataaccess.impl.user.accounts;

import gov.nara.opa.api.valueobject.administrator.AccountMaintenanceValueObject;
import gov.nara.opa.api.valueobject.administrator.AccountMaintenanceValueObjectConstants;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class AutoDisableUserAccountExtractor implements 
	ResultSetExtractor<AccountMaintenanceValueObject>, AccountMaintenanceValueObjectConstants {

	private static OpaLogger logger = OpaLogger.getLogger(AutoDisableUserAccountExtractor.class);

	@Override
	public AccountMaintenanceValueObject extractData(ResultSet rs) 
			throws SQLException, DataAccessException {
		AccountMaintenanceValueObject result = new AccountMaintenanceValueObject();
		try {
			result.setAccountId(rs.getInt(ACCOUNT_ID_DB));
			result.setEmail(rs.getString(EMAIL_ADDRESS_DB));
			result.setRemaining(rs.getInt(REMAINING_DB));
			result.setActionType(rs.getString(ACTION_TYPE_DB));
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			throw new InvalidDataAccessApiUsageException(e.getMessage());
		}
		return result;
	}

}
