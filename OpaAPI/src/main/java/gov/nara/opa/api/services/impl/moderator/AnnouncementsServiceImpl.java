package gov.nara.opa.api.services.impl.moderator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.nara.opa.api.dataaccess.moderator.AnnouncementsDao;
import gov.nara.opa.api.services.moderator.AnnouncementsService;
import gov.nara.opa.api.validation.moderator.UpdateAnnouncementsRequestParameters;
import gov.nara.opa.api.valueobject.moderator.AnnouncementValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;

@Component
@Transactional
public class AnnouncementsServiceImpl implements AnnouncementsService {

	private static OpaLogger logger = OpaLogger
			.getLogger(AnnouncementsServiceImpl.class);

	@Autowired
	private AnnouncementsDao announcementsDao;

	public AnnouncementValueObject getAnnouncement() {
		logger.info("Getting announcements");
		return announcementsDao.getAnnouncement();
	}

	public AnnouncementValueObject getAnnouncementForModerator() {
		logger.info("Getting announcements for moderator");
		return announcementsDao.getAnnouncementForModerator();
	}

	public AnnouncementValueObject updateAnnouncement(
			UpdateAnnouncementsRequestParameters requestParameters) {
		logger.info("Updating announcement");
		return announcementsDao.updateAnnouncement(requestParameters.getText(),
				requestParameters.getEnabled());
	}
}
