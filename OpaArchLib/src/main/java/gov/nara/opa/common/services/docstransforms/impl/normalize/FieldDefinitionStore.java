package gov.nara.opa.common.services.docstransforms.impl.normalize;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.exception.OpaSkipRecordException;
import gov.nara.opa.common.dataaccess.export.FieldDefinitionDao;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.valueobject.export.FieldDefinitionDbValueObject;
import gov.nara.opa.common.valueobject.export.FieldDefinitionValueObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class FieldDefinitionStore implements InitializingBean, Constants {

  public static final List<String> FIELD_TYPES = new ArrayList<String>();
  static {
    FIELD_TYPES.add(FIELD_TYPE_BOOLEAN);
    FIELD_TYPES.add(FIELD_TYPE_BOOLEAN_LIST);
    FIELD_TYPES.add(FIELD_TYPE_FIELD_LIST);
    FIELD_TYPES.add(FIELD_TYPE_INTEGER);
    FIELD_TYPES.add(FIELD_TYPE_INTEGER_LIST);
    FIELD_TYPES.add(FIELD_TYPE_STRING);
    FIELD_TYPES.add(FIELD_TYPE_STRING_LIST);
  }

  public static final List<String> FIELD_SOURCES = new ArrayList<String>();
  static {
    FIELD_SOURCES.add(FIELD_SOURCE_HARDCODED);
    FIELD_SOURCES.add(FIELD_SOURCE_OPA_XML_FIELDS_LIST_XSL);
    FIELD_SOURCES.add(FIELD_SOURCE_OPA_XML_LIST_XSL);
    FIELD_SOURCES.add(FIELD_SOURCE_OPA_XML_SIMPLE_XPATH);
    FIELD_SOURCES.add(FIELD_SOURCE_OPA_XML_SINGLE_VALUE_XSL);
    FIELD_SOURCES.add(FIELD_SOURCE_THUMBNAILS);
    FIELD_SOURCES.add(FIELD_SOURCE_TRANSCRIPTIONS);
    FIELD_SOURCES.add(FIELD_SOURCE_SOLR_DOC);
    FIELD_SOURCES.add(FIELD_SOURCE_TAGS);
  }

  @Autowired
  FieldDefinitionDao fieldDefinitionDao;

  @Autowired
  DocumentValuesExtractorHelper valuesExtractorHelper;

  private boolean isLoaded;

  private List<FieldDefinitionDbValueObject> allFieldsDb;

  private List<FieldDefinitionDbValueObject> allFieldsBriefDb;

  public boolean isLoaded() {
    return isLoaded;
  }

  Map<String, Map<String, LinkedList<FieldDefinitionValueObject>>> FIELDS_STORE = new ConcurrentHashMap<String, Map<String, LinkedList<FieldDefinitionValueObject>>>();

  @Override
  public void afterPropertiesSet() throws Exception {

    allFieldsBriefDb = fieldDefinitionDao.getAllFieldsDefinitionsBrief();
    FIELDS_STORE.put(EXPORT_TYPE_BRIEF,
        getFieldsDefinitions(allFieldsBriefDb, null, true));

    allFieldsDb = fieldDefinitionDao.getAllFieldsDefinitions();
    FIELDS_STORE.put(EXPORT_TYPE_FULL,
        getFieldsDefinitions(allFieldsDb, null, false));

    isLoaded = true;
  }

  private Map<String, LinkedList<FieldDefinitionValueObject>> getFieldsDefinitions(
      List<FieldDefinitionDbValueObject> fieldsDefinitionsDb,
      List<String> fieldNames, boolean brief) {
    Map<String, LinkedList<FieldDefinitionValueObject>> fieldsDefinitions = new ConcurrentHashMap<String, LinkedList<FieldDefinitionValueObject>>();

    for (FieldDefinitionDbValueObject fieldDefinitionDb : fieldsDefinitionsDb) {
      if (fieldNames == null
          || (fieldNames != null && fieldNames.contains(fieldDefinitionDb
              .getFieldName()))) {

        FieldDefinitionValueObject fieldDefinition = getShortFieldDefinition(
            fieldDefinitionDb, brief);
        validateShortFieldDefinition(fieldDefinition);
        addFieldDefinitionToMap(fieldsDefinitions,
            fieldDefinitionDb.getResultType(), fieldDefinition);
      }
    }
    return fieldsDefinitions;
  }

  private void addFieldDefinitionToMap(
      Map<String, LinkedList<FieldDefinitionValueObject>> fieldsDefinitions,
      String resultType, FieldDefinitionValueObject fieldDefinition) {

    if (!fieldsDefinitions.containsKey(resultType)) {
      fieldsDefinitions.put(resultType,
          new LinkedList<FieldDefinitionValueObject>());
    }
    fieldsDefinitions.get(resultType).add(fieldDefinition);
  }

  private FieldDefinitionValueObject getShortFieldDefinition(
      FieldDefinitionDbValueObject definitionDb, boolean brief) {
    FieldDefinitionValueObject definition = new FieldDefinitionValueObject();
    definition.setName(definitionDb.getFieldName());
    definition.setSource(definitionDb.getFieldSource());
    definition.setValueGenerationInstruction(definitionDb
        .getFieldValueInstruction());
    definition.setSection(definitionDb.getSection());
    definition.setType(definitionDb.getFieldType());
    definition.setId(definitionDb.getFieldId());
    if (brief) {
      definition.setLabel(definitionDb.getFieldLabelBrief());
    } else {
      definition.setLabel(definitionDb.getFieldLabel());
    }
    definition.setValueExtractor(valuesExtractorHelper
        .getValueExtractor(definition));
    ;
    return definition;
  }

  public LinkedList<FieldDefinitionValueObject> getFieldDefinitionsForSpecificFields(
      String exportId, List<String> fieldNames, String resultType) {
    if (FIELDS_STORE.containsKey(exportId)) {
      return getFieldDefinitions(exportId, resultType);
    }
    FIELDS_STORE.put(exportId,
        getFieldsDefinitions(allFieldsDb, fieldNames, false));
    return getFieldDefinitions(exportId, resultType);
  }

  public void removeFieldDefinitionsForSpecificFields(String exportId) {
    FIELDS_STORE.remove(exportId);
  }

  public LinkedList<FieldDefinitionValueObject> getFieldDefinitions(
      String exportType, String resultType) {
    if (!isLoaded()) {
      throw new OpaRuntimeException(
          "The field definition store was not loaded!");
    }
    if (!FIELDS_STORE.containsKey(exportType)) {
      throw new OpaRuntimeException(
          "No field definitions are loaded for this export type: " + exportType);
    }
    Map<String, LinkedList<FieldDefinitionValueObject>> fieldDefinitionsByResultType = FIELDS_STORE
        .get(exportType);
    if (!fieldDefinitionsByResultType.containsKey(resultType)) {
      throw new OpaSkipRecordException(
          String
              .format(
                  "No field definitions exist for export type %1$s and result type %2$s",
                  exportType, resultType));
    }
    return fieldDefinitionsByResultType.get(resultType);
  }

  private void validateShortFieldDefinition(
      FieldDefinitionValueObject fieldDefinition) {
    // TODO add a bunch of validations here to help ensure data integrity;
  }
}
