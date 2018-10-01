package gov.nara.opa.common.dataaccess.impl.export;

import gov.nara.opa.common.valueobject.export.FieldDefinitionDbValueObject;
import gov.nara.opa.common.valueobject.export.FieldDefinitionDbValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class FieldDefinitionDbValueObjectExtractor implements
    ResultSetExtractor<FieldDefinitionDbValueObject>,
    FieldDefinitionDbValueObjectConstants {

  @Override
  public FieldDefinitionDbValueObject extractData(ResultSet rs)
      throws SQLException {
    FieldDefinitionDbValueObject row = new FieldDefinitionDbValueObject();

    row.setFieldId(rs.getInt(FIELD_ID_DB));
    row.setFieldLabel(rs.getString(FIELD_LABEL_DB));
    row.setFieldLabelBrief(rs.getString(FIELD_LABEL_BRIEF_DB));
    row.setFieldName(rs.getString(FIELD_NAME_DB));
    row.setFieldSource(rs.getString(FIELD_SOURCE_DB));
    row.setFieldValueInstruction(rs.getString(FIELD_VALUE_DB));
    row.setOrderNumber(rs.getInt(ORDER_NUMBER_DB));
    row.setOrderNumberBrief(rs.getInt(ORDER_NUMBER_BRIEF_DB));
    row.setResultType(rs.getString(RESULT_TYPE_DB));
    row.setSection(rs.getString(SECTION_DB));
    row.setFieldType(rs.getString(FIELD_TYPE_DB));
    return row;
  }

}
