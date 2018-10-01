package gov.nara.opa.api.dataaccess.system;

import gov.nara.opa.api.valueobject.system.LanguageCollectionValueObject;

public interface LanguagesDao {

	LanguageCollectionValueObject retrieveLanguagesFromTable();
}
