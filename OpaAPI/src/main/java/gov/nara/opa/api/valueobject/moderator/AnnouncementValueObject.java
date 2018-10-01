package gov.nara.opa.api.valueobject.moderator;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnnouncementValueObject extends AbstractWebEntityValueObject
		implements AnnouncementValueObjectConstants {

	private Integer announcementId;
	private String announcement;
	private Boolean status;
	private Timestamp announcementTS;

	public Integer getAnnouncementId() {
		return announcementId;
	}

	public void setAnnouncementId(Integer announcementId) {
		this.announcementId = announcementId;
	}

	public String getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Timestamp getAnnouncementTS() {
		return announcementTS;
	}

	public void setAnnouncementTS(Timestamp announcementTS) {
		this.announcementTS = announcementTS;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new LinkedHashMap<String, Object>();
		return databaseContent;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		aspireContent.put(ANNOUNCEMENT_TEXT_ASP, getAnnouncement());

		if (action.equals(VIEW_ANNOUNCEMENT_MODERATOR)
				|| action
						.endsWith(AnnouncementValueObjectConstants.UPDATE_ANNOUNCEMENT)) {
			aspireContent.put(ANNOUNCEMENT_ENABLED_ASP, getStatus());
			if (getAnnouncementTS() != null) {
				aspireContent.put(ANNOUNCEMENT_TS_ASP, getAnnouncementTS());
			}
		}

		return aspireContent;
	}
}
