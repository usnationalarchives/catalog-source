package gov.nara.opa.api.dataaccess.impl.system;

import gov.nara.opa.api.dataaccess.system.LanguagesDao;
import gov.nara.opa.api.valueobject.system.LanguageCollectionValueObject;
import gov.nara.opa.api.valueobject.system.LanguageValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class LanguagesJDBCTemplate extends AbstractOpaDbJDBCTemplate implements 
	LanguagesDao {

	@SuppressWarnings("unchecked")
	@Override
	public LanguageCollectionValueObject retrieveLanguagesFromTable() {
		return new LanguageCollectionValueObject(
				  (List<LanguageValueObject>) StoredProcedureDataAccessUtils
				  .execute(getJdbcTemplate(), "spGetLanguages", 
						  new GenericRowMapper<LanguageValueObject>(new LanguageResultSetExtractor()), 
						  null)
				  );
	}

}