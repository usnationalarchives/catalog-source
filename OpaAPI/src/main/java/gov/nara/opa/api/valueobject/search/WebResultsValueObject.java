package gov.nara.opa.api.valueobject.search;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebResultsValueObject extends AbstractWebEntityValueObject
		implements WebResultsObjectConstants {

	int docNumber;
	Float score;
	String opaId;
	String title;
	String webArea;
	String webAreaUrl;
	String url;
	String iconType;
	String teaser;
	String extractedText;

	public String getExtractedText() {
		return extractedText;
	}

	public void setExtractedText(String extractText) {
		this.extractedText = extractText;
	}

	public int getDocNumber() {
		return docNumber;
	}

	public void setDocNumber(int docNumber) {
		this.docNumber = docNumber;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public String getOpaId() {
		return opaId;
	}

	public void setOpaId(String opaId) {
		this.opaId = opaId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWebArea() {
		return webArea;
	}

	public void setWebArea(String webArea) {
		this.webArea = webArea;
	}

	public String getWebAreaUrl() {
		return webAreaUrl;
	}

	public void setWebAreaUrl(String webAreaUrl) {
		this.webAreaUrl = webAreaUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIconType() {
		return iconType;
	}

	public void setIconType(String iconType) {
		this.iconType = iconType;
	}

	public String getTeaser() {
		return teaser;
	}

	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {

		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

		aspireContent.put(NUM_ASP, getDocNumber());
		aspireContent.put(SCORE_ASP, getScore());
		aspireContent.put(OPA_ID_ASP, getOpaId());

		if (getTitle() != null) {
			aspireContent.put(TITLE_ASP, getTitle());
		}
		if (getWebArea() != null) {
			aspireContent.put(WEB_AREA_ASP, getWebArea());
		}
		if (getWebAreaUrl() != null) {
			aspireContent.put(WEB_AREA_URL_ASP, getWebAreaUrl());
		}
		if (getUrl() != null) {
			aspireContent.put(URL_ASP, getUrl());
		}
		if (getIconType() != null) {
			aspireContent.put(ICON_TYPE_ASP, getIconType());
		}
		if (getTeaser() != null) {
			aspireContent.put(TEASER_ASP, getTeaser());
		}
		if (getExtractedText() != null) {
			aspireContent.put(EXTRACT_TEXT, getExtractedText());
		}

		return aspireContent;

	}

}
