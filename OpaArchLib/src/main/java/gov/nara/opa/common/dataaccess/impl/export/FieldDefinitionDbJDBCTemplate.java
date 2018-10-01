package gov.nara.opa.common.dataaccess.impl.export;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.export.FieldDefinitionDao;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.common.valueobject.export.FieldDefinitionDbValueObject;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FieldDefinitionDbJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements FieldDefinitionDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<FieldDefinitionDbValueObject> getAllFieldsDefinitions() {
		return (List<FieldDefinitionDbValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAllFieldsDefinitions",
						new FieldDefinitionDbValueObjectRowMapper(), null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FieldDefinitionDbValueObject> getAllFieldsDefinitionsBrief() {
		return (List<FieldDefinitionDbValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAllFieldsDefinitionsBrief",
						new FieldDefinitionDbValueObjectRowMapper(), null);
	}

}
