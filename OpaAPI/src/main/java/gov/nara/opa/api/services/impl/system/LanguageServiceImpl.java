package gov.nara.opa.api.services.impl.system;

import gov.nara.opa.api.dataaccess.system.LanguagesDao;
import gov.nara.opa.api.services.system.LanguageService;
import gov.nara.opa.api.valueobject.system.LanguageCollectionValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LanguageServiceImpl implements LanguageService {

	@Autowired
	private LanguagesDao languagesDao;

	@Override
	public LanguageCollectionValueObject getLanguages() {
		return languagesDao.retrieveLanguagesFromTable();
	}

}
