package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderActionValueObject;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class OnlineAvailabilityHeaderActionValueObjectExtractor implements
		ResultSetExtractor<OnlineAvailabilityHeaderActionValueObject>,
		OnlineAvailabilityHeaderValueObjectConstants {

	@Override
	public OnlineAvailabilityHeaderActionValueObject extractData(ResultSet rs)
			throws SQLException {
		// comment specific fields
		OnlineAvailabilityHeaderActionValueObject onlineAvailabilityHeaderAction = new OnlineAvailabilityHeaderActionValueObject();
		onlineAvailabilityHeaderAction.setUserName(rs.getString(USER_NAME_DB));
		onlineAvailabilityHeaderAction.setFullName(rs.getString(FULL_NAME_DB));
		onlineAvailabilityHeaderAction.setDisplayFullName(rs
				.getBoolean(DISPLAY_NAME_FLAG_DB));
		onlineAvailabilityHeaderAction.setIsNaraStaff(rs
				.getBoolean(IS_NARA_STAFF_DB));
		onlineAvailabilityHeaderAction.setAction(rs.getString(ACTION_DB));
		onlineAvailabilityHeaderAction.setActionTS(rs.getTimestamp(LOG_TS_DB));

		return onlineAvailabilityHeaderAction;
	}
}
