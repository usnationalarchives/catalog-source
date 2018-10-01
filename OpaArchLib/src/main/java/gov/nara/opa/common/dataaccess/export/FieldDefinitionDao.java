package gov.nara.opa.common.dataaccess.export;

import gov.nara.opa.common.valueobject.export.FieldDefinitionDbValueObject;

import java.util.List;

public interface FieldDefinitionDao {

  List<FieldDefinitionDbValueObject> getAllFieldsDefinitions();

  List<FieldDefinitionDbValueObject> getAllFieldsDefinitionsBrief();
}
