package gov.nara.opa.api.services.moderator;

import gov.nara.opa.api.validation.moderator.UpdateAnnouncementsRequestParameters;
import gov.nara.opa.api.valueobject.moderator.AnnouncementValueObject;

public interface AnnouncementsService {

	AnnouncementValueObject getAnnouncement();

	AnnouncementValueObject getAnnouncementForModerator();

	AnnouncementValueObject updateAnnouncement(
			UpdateAnnouncementsRequestParameters requestParameters);
}
