package gov.nara.opa.api.dataaccess.impl.system;

import gov.nara.opa.api.valueobject.system.LanguageValueObject;
import gov.nara.opa.api.valueobject.system.LanguageValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class LanguageResultSetExtractor implements 
	ResultSetExtractor<LanguageValueObject>, LanguageValueObjectConstants {

	@Override
	public LanguageValueObject extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		LanguageValueObject language = new LanguageValueObject();
		language.setIsoCode(rs.getString(ISO_CODE_DB));
		language.setLanguage(rs.getString(LANGUAGE_DB));
		return language;
	}

}
