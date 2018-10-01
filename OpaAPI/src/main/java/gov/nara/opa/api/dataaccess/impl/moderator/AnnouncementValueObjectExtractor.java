package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.valueobject.moderator.AnnouncementValueObject;
import gov.nara.opa.api.valueobject.moderator.AnnouncementValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class AnnouncementValueObjectExtractor implements
		ResultSetExtractor<AnnouncementValueObject>,
		AnnouncementValueObjectConstants {

	@Override
	public AnnouncementValueObject extractData(ResultSet rs)
			throws SQLException {
		// comment specific fields
		AnnouncementValueObject announcement = new AnnouncementValueObject();
		announcement.setAnnouncementId(rs.getInt(ANNOUNCEMENT_ID_DB));
		announcement.setAnnouncement(rs.getString(ANNOUNCEMENT_DB));
		announcement.setStatus(rs.getBoolean(STATUS_DB));
		announcement.setAnnouncementTS(rs.getTimestamp(ANNOUNCEMENT_TS_DB));

		return announcement;
	}
}