package gov.nara.opa.common.services.docstransforms.impl.normalize;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.FieldDefinitionValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractValueExtractor {

  @SuppressWarnings("unchecked")
  public ValueHolderValueObject extractValue(
      FieldDefinitionValueObject fieldDefinition,
      SearchRecordValueObject document, AccountExportValueObject accountExport) {
    Object value = getValue(fieldDefinition.getValueGenerationInstruction(),
        document, fieldDefinition.getType(), accountExport,
        fieldDefinition.getSection());
    if (value == null || (value instanceof List && ((List<?>) value).size() == 0)) {
      return null;
    }
    ValueHolderValueObject valueHolder = new ValueHolderValueObject();
    valueHolder.setName(fieldDefinition.getName());
    valueHolder.setLabel(fieldDefinition.getLabel());
    valueHolder.setSection(fieldDefinition.getSection());
    if (value instanceof List
        && ((List<?>) value).get(0) instanceof ValueHolderValueObject) {
      valueHolder
          .setArrayOfObjectsValue((LinkedList<ValueHolderValueObject>) value);
    } else {
      valueHolder.setValue(value);
    }
    valueHolder.setType(fieldDefinition.getType());
    return valueHolder;
  }

  protected abstract Object getValue(String valueGenerationInstruction,
      SearchRecordValueObject document, String type,
      AccountExportValueObject accountExport, String section);

  protected static Object getCastedObjectValue(Object value, String type) {
    if (value == null) {
      return null;
    }

    if (type.equals("string")) {
      return value.toString();
    } else if (type.equals("integer")) {
      return Integer.valueOf(value.toString());
    } else if (type.equals("boolean")) {
      return Boolean.valueOf(value.toString());
    }
    throw new OpaRuntimeException(
        "Can not make a conversion to specific object time. Unkown field type "
            + type);
  }

}
