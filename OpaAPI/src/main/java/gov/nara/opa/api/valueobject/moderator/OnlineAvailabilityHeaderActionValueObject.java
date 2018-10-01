package gov.nara.opa.api.valueobject.moderator;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class OnlineAvailabilityHeaderActionValueObject extends
		AbstractWebEntityValueObject implements
		OnlineAvailabilityHeaderValueObjectConstants {

	private String userName;
	private String fullName;
	private Boolean displayFullName;
	private Boolean isNaraStaff;
	private String action;
	private Timestamp actionTS;

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getDisplayFullName() {
		return this.displayFullName;
	}

	public void setDisplayFullName(Boolean displayFullName) {
		this.displayFullName = displayFullName;
	}

	public Boolean getIsNaraStaff() {
		return this.isNaraStaff;
	}

	public void setIsNaraStaff(Boolean isNaraStaff) {
		this.isNaraStaff = isNaraStaff;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Timestamp getActionTS() {
		return this.actionTS;
	}

	public void setActionTS(Timestamp actionTS) {
		this.actionTS = actionTS;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new LinkedHashMap<String, Object>();
		return databaseContent;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

		aspireContent
				.put(ONLINE_AVAILABILITY_HEADER_USER_ID_ASP, getUserName());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_FULL_NAME_ASP,
				getFullName());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_DISPLAY_NAME_FLAG_ASP,
				getDisplayFullName());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_IS_NARA_STAFF_ASP,
				getIsNaraStaff());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_ACTION_ASP, getAction());
		aspireContent.put(ONLINE_AVAILABILITY_HEADER_ACTION_TS_ASP,
				getActionTS());

		return aspireContent;
	}

}
