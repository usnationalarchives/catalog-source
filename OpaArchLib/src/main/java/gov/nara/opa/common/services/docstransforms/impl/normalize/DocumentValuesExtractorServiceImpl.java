package gov.nara.opa.common.services.docstransforms.impl.normalize;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.services.docstransforms.DocumentValuesExtractorService;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;
import gov.nara.opa.common.valueobject.export.FieldDefinitionValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObjectHelper;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class DocumentValuesExtractorServiceImpl implements
    DocumentValuesExtractorService, Constants {

  @Autowired
  FieldDefinitionStore fieldDefinitionStore;

  OpaLogger logger = OpaLogger
      .getLogger(DocumentValuesExtractorServiceImpl.class);

  @Override
  public LinkedList<ValueHolderValueObject> getValues(
      SearchRecordValueObject document, AccountExportValueObject accountExport) {

    String resultType = SearchRecordValueObjectHelper.getResultType(document);
    document.setResultType(resultType);
    LinkedList<FieldDefinitionValueObject> fieldDefinitions = getFieldDefinitions(
        accountExport, resultType);
    return getValues(fieldDefinitions, document, accountExport);
  }

  private LinkedList<ValueHolderValueObject> getValues(
      LinkedList<FieldDefinitionValueObject> fieldDefinitions,
      SearchRecordValueObject document, AccountExportValueObject accountExport) {

    LinkedList<ValueHolderValueObject> values = new LinkedList<ValueHolderValueObject>();

    for (FieldDefinitionValueObject fieldDefinition : fieldDefinitions) {
      if (!includeMetadataField(accountExport, fieldDefinition.getName())) {
        continue;
      }
      AbstractValueExtractor valueExtractor = fieldDefinition
          .getValueExtractor();
      if (valueExtractor == null) {
        logger.fatal(String.format(
            "Can't find a value extractor for field name %1$s / field id %2$d",
            fieldDefinition.getName(), fieldDefinition.getId()));
        continue;
      }
      ValueHolderValueObject value = fieldDefinition.getValueExtractor()
          .extractValue(fieldDefinition, document, accountExport);

      if (value == null) {
        continue;
      }

      values.add(value);
    }
    return values;
  }

  public boolean includeMetadataField(AccountExportValueObject accountExport,
      String fieldName) {
    if (!accountExport.getIncludeMetadata().booleanValue()
        && !fieldName.equals(FIELD_SOURCE_TAGS)
        && !fieldName.equals(FIELD_SOURCE_THUMBNAILS)
        && !fieldName.equals(FIELD_SOURCE_TRANSCRIPTIONS)) {
      return false;
    }
    return true;
  }

  private LinkedList<FieldDefinitionValueObject> getFieldDefinitions(
      AccountExportValueObject accountExport, String resultType) {
    String exportType = accountExport.getExportType();
    if (exportType.equals(EXPORT_TYPE_BRIEF)
        || exportType.equals(EXPORT_TYPE_FULL)) {
      return fieldDefinitionStore.getFieldDefinitions(exportType, resultType);
    } else if (exportType.equals(EXPORT_TYPE_FIELDS)) {
      return fieldDefinitionStore.getFieldDefinitionsForSpecificFields(
          accountExport.getExportId().toString(),
          AccountExportValueObjectHelper.getResultFields(accountExport),
          resultType);
    }
    throw new OpaRuntimeException("Unknown export type: " + exportType);
  }
}
