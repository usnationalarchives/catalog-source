package gov.nara.opa.common.services.docstransforms;

import gov.nara.opa.common.ResultTypeConstants;

public interface Constants extends ResultTypeConstants {

  public static final String EXPORT_TYPE_BRIEF = "brief";
  public static final String EXPORT_TYPE_FULL = "full";
  public static final String EXPORT_TYPE_FIELDS = "fields";

  public static final String EXPORT_FORMAT_TEXT = "txt";
  public static final String EXPORT_FORMAT_PDF = "pdf";
  public static final String EXPORT_FORMAT_JSON = "json";
  public static final String EXPORT_FORMAT_XML = "xml";
  public static final String EXPORT_FORMAT_CSV = "csv";
  public static final String EXPORT_FORMAT_PRINT = "print";

  public static final String RESULT_TYPE_OBJECT = "object";

  public static final String FIELD_SOURCE_HARDCODED = "hardcoded";
  public static final String FIELD_SOURCE_SOLR_DOC = "solrdoc";
  public static final String FIELD_SOURCE_LINK = "link";
  public static final String FIELD_SOURCE_OPA_XML_SIMPLE_XPATH = "opaXmlSimpleXpath";
  public static final String FIELD_SOURCE_OPA_XML_LIST_XSL = "opaXmlListXsl";
  public static final String FIELD_SOURCE_OPA_XML_SINGLE_VALUE_XSL = "opaXmlSingleValueXsl";
  public static final String FIELD_SOURCE_OPA_XML_FIELDS_LIST_XSL = "opaXmlFieldsListXsl";
  public static final String FIELD_SOURCE_TAGS = "tags";
  public static final String FIELD_SOURCE_TRANSCRIPTIONS = "transcriptions";
  public static final String FIELD_SOURCE_THUMBNAILS = "thumbnails";

  public static final String FIELD_TYPE_INTEGER = "integer";
  public static final String FIELD_TYPE_STRING = "string";
  public static final String FIELD_TYPE_BOOLEAN = "boolean";
  public static final String FIELD_TYPE_STRING_LIST = "stringList";
  public static final String FIELD_TYPE_INTEGER_LIST = "stringInteger";
  public static final String FIELD_TYPE_BOOLEAN_LIST = "stringBoolean";
  public static final String FIELD_TYPE_FIELD_LIST = "fieldList";

  public static final String PRINTING_FORMAT_PRETTY_TRUE = "PRETTY_TRUE";
  public static final String PRINTING_FORMAT_PRETTY_FALSE = "PRETTY_FALSE";
  public static final String PRINTING_RECORD_LINE = "RECORD_LINE";

}
