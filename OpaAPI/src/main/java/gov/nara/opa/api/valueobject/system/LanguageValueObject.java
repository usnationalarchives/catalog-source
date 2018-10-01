package gov.nara.opa.api.valueobject.system;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.api.valueobject.system.LanguageValueObjectConstants;

public class LanguageValueObject extends AbstractWebEntityValueObject implements 
	LanguageValueObjectConstants {

	private String isoCode;
	private String language;

	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new HashMap<String, Object>();

		databaseContent.put(ISO_CODE_DB, getIsoCode());
		databaseContent.put(LANGUAGE_DB, getLanguage());

		return databaseContent;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

		aspireContent.put(ISO_CODE_ASP, getIsoCode());
		aspireContent.put(LANGUAGE_ASP, getLanguage());

		return aspireContent;
	}

}
