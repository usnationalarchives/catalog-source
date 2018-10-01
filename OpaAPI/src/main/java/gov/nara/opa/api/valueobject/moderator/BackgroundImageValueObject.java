package gov.nara.opa.api.valueobject.moderator;

import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class BackgroundImageValueObject extends AbstractWebEntityValueObject
		implements BackgroundImageValueObjectConstants {

	private String naId;
	private String objectId;
	private String title;
	private String url;
	private Boolean isDefault = false;

	public String getNaId() {
		return this.naId;
	}

	public void setNaId(String naId) {
		this.naId = naId;
	}

	public String getObjectId() {
		return this.objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getIsDefault() {
		return this.isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new LinkedHashMap<String, Object>();
		return databaseContent;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		aspireContent.put(BACKGROUND_IMAGE_NA_ID_ASP, getNaId());
		aspireContent.put(BACKGROUND_IMAGE_OBJECT_ID_ASP, getObjectId());
		aspireContent.put(BACKGROUND_IMAGE_TITLE_ASP, getTitle());
		aspireContent.put(BACKGROUND_IMAGE_PATH_ASP, getUrl());
		return aspireContent;
	}
}
