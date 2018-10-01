package gov.nara.opa.api.valueobject.system;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LanguageCollectionValueObject extends AbstractWebEntityValueObject 
	implements LanguageValueObjectConstants {

	private List<LanguageValueObject> languages;
	private int totalLanguages;
	private String entityName;

	@Override
	public Map<String, Object> getDatabaseContent() {
		return null;
	}

	public LanguageCollectionValueObject(List<LanguageValueObject> languages) {
	    if (languages == null) {
	      throw new OpaRuntimeException("The languages parameter cannot be null");
	    }
	    this.languages = languages;
	    if (languages != null) {
	    	totalLanguages = languages.size();
	    	entityName = "languages";
	    }
	  }

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		if (languages != null && languages.size() > 0) {
			aspireContent.put("@total", totalLanguages);
			aspireContent.put("languages", languages);
		}
		return aspireContent;
	}

	public Integer getTotalLanguages() {
	    return totalLanguages;
	}

	public List<LanguageValueObject> getLanguages() {
	    return languages;
	}

	public String getEntityName() {
		return entityName;
	}
}
