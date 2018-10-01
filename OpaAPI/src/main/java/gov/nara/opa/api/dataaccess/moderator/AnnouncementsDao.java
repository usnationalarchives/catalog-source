package gov.nara.opa.api.dataaccess.moderator;

import gov.nara.opa.api.valueobject.moderator.AnnouncementValueObject;

public interface AnnouncementsDao {

	AnnouncementValueObject getAnnouncement();

	AnnouncementValueObject getAnnouncementForModerator();

	AnnouncementValueObject updateAnnouncement(String announcement,
			Boolean enabled);
}
