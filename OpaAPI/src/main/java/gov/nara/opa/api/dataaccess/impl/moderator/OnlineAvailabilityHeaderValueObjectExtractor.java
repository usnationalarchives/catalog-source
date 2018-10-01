package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObject;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class OnlineAvailabilityHeaderValueObjectExtractor implements
		ResultSetExtractor<OnlineAvailabilityHeaderValueObject>,
		OnlineAvailabilityHeaderValueObjectConstants {

	@Override
	public OnlineAvailabilityHeaderValueObject extractData(ResultSet rs)
			throws SQLException {
		// comment specific fields
		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = new OnlineAvailabilityHeaderValueObject();
		onlineAvailabilityHeader.setNaId(rs.getString(NA_ID_DB));
		onlineAvailabilityHeader.setTitle(rs.getString(TITLE_DB));
		onlineAvailabilityHeader.setHeader(rs.getString(HEADER_DB));
		onlineAvailabilityHeader.setStatus(rs.getBoolean(STATUS_DB));
		onlineAvailabilityHeader.setAvailabilityTS(rs
				.getTimestamp(ONLINE_AVAILABILITY_TS_DB));

		return onlineAvailabilityHeader;
	}
}