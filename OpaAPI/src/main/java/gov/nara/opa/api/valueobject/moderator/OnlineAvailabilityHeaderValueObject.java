package gov.nara.opa.api.valueobject.moderator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class OnlineAvailabilityHeaderValueObject extends
		AbstractWebEntityValueObject implements
		OnlineAvailabilityHeaderValueObjectConstants {

	private String naId;
	private String title;
	private String header;
	private Boolean status;
	private Timestamp availabilityTS;
	private List<OnlineAvailabilityHeaderActionValueObject> actions = new ArrayList<OnlineAvailabilityHeaderActionValueObject>();

	public String getNaId() {
		return this.naId;
	}

	public void setNaId(String naId) {
		this.naId = naId;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHeader() {
		return this.header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public Boolean getStatus() {
		return this.status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Timestamp getAvailabilityTS() {
		return this.availabilityTS;
	}

	public void setAvailabilityTS(Timestamp availabilityTS) {
		this.availabilityTS = availabilityTS;
	}

	public List<OnlineAvailabilityHeaderActionValueObject> getActions() {
		return this.actions;
	}

	public void setActions(
			List<OnlineAvailabilityHeaderActionValueObject> actions) {
		this.actions = actions;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new LinkedHashMap<String, Object>();
		return databaseContent;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

		aspireContent.put(ONLINE_AVAILABILITY_HEADER_NA_ID_ASP, getNaId());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_TITLE_ASP, getTitle());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_HEADER_ASP, getHeader());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_STATUS_ASP, getStatus());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_TIMESTAMP_ASP,
				getAvailabilityTS());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_ACTIONS_ASP, getActions());
		return aspireContent;
	}

}
