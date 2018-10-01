package gov.nara.opa.common.valueobject.annotation.translations;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.AnnotationConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TranslationsCollectionValueObject extends AbstractWebEntityValueObject 
	implements CommonValueObjectConstants {

	List<TranslationValueObject> translations;
	int totalTranslations;

	public TranslationsCollectionValueObject(List<TranslationValueObject> translations) {
		if (translations == null) {
			throw new OpaRuntimeException(
					"The translations parameter cannot be null");
		}
		this.translations = translations;
		this.totalTranslations = translations.size();
	}

	public int getTotalTranslations() {
		return totalTranslations;
	}

	public List<TranslationValueObject> getTranslations() {
		return translations;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = 
				new LinkedHashMap<String, Object>();
		if (translations != null && translations.size() > 0) {
			aspireContent.put(TOTAL_RECORDS_ASP, totalTranslations);
			aspireContent.put(AnnotationConstants.TRANSLATIONS, translations);
		}
		return aspireContent;
	}

}
