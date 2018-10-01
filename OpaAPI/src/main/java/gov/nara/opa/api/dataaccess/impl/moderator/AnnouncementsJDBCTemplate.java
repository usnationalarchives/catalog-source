package gov.nara.opa.api.dataaccess.impl.moderator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.api.dataaccess.moderator.AnnouncementsDao;
import gov.nara.opa.api.valueobject.moderator.AnnouncementValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AnnouncementsJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements AnnouncementsDao {

	@SuppressWarnings("unchecked")
	@Override
	public AnnouncementValueObject getAnnouncement() {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		List<AnnouncementValueObject> announcements = (List<AnnouncementValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnouncement",
						new GenericRowMapper<AnnouncementValueObject>(
								new AnnouncementValueObjectExtractor()),
						inParamMap);

		AnnouncementValueObject theAnnouncement = null;
		if (announcements != null && announcements.size() > 0) {
			theAnnouncement = announcements.get(0);
		}

		return theAnnouncement;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnnouncementValueObject getAnnouncementForModerator() {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		List<AnnouncementValueObject> announcements = (List<AnnouncementValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnouncementForModerator",
						new GenericRowMapper<AnnouncementValueObject>(
								new AnnouncementValueObjectExtractor()),
						inParamMap);

		AnnouncementValueObject theAnnouncementForModerator = null;
		if (announcements != null && announcements.size() > 0) {
			theAnnouncementForModerator = announcements.get(0);
		}

		return theAnnouncementForModerator;
	}

	@Override
	public AnnouncementValueObject updateAnnouncement(String announcement,
			Boolean enabled) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("announcementText", announcement);
		inParamMap.put("announcementStatus", enabled);

		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spUpdateAnnouncement", inParamMap);

		AnnouncementValueObject theAnnouncementForModerator = getAnnouncementForModerator();

		return theAnnouncementForModerator;
	}
}
