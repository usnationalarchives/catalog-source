package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.validation.export.CreateAccountExportRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvMapWriter;

@Component
@Scope("prototype")
public class CsvDocumentCreator extends AbstractDocumentCreator implements
    DocumentCreator, Constants {
	
  private static OpaLogger logger = OpaLogger.getLogger(CsvDocumentCreator.class);
  
  private static final String characterSeparator = "|";

  public static final Map<String, Integer> DEFAULT_FIELDS_INDEX_MAP = new LinkedHashMap<String, Integer>();
  static {
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_RECORD_GROUP, 0);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_COLLECTION, 1);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_SERIES, 2);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_FILE_UNIT, 3);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_ITEM, 4);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_PERSON, 5);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_ORGANIZATION, 6);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_GEOGRAPHIC_REFERENCE, 7);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_TOPICAL_SUBJECT, 8);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_SPECIFIC_RECORDS_TYPE, 9);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_ARCHIVES_WEB, 10);
    DEFAULT_FIELDS_INDEX_MAP.put(RESULT_TYPE_PRESIDENTIAL_WEB, 11);

  }

  @Override
  public void createDocument(LinkedList<ValueHolderValueObject> records,
      OutputStream outputStream, int documentIndex, int totalDocuments,
      boolean flush, String resultType, AccountExportValueObject accountExport,
      Object writer, SearchRecordValueObject document,
      SolrQueryResponseValueObject queryResponse) throws IOException {

    CsvMapWriter csvWriter = (CsvMapWriter) writer;

    String[] headers = getHeaders(accountExport);

    if (documentIndex == 1) {
      // force a reinitialization of member variables
      csvWriter.writeHeader(getHeaders(accountExport));
    }
    if (records == null) {
      csvWriter.flush();
      if (flush) {
        outputStream.flush();
      }
      return;
    }
    try {
      writeRecord(csvWriter, records, resultType, headers, accountExport,
          document);
    } catch (IOException ex) {
      throw new OpaRuntimeException(ex);
    }

    csvWriter.flush();
    if (flush) {
      outputStream.flush();
    }
  }

  private void writeRecord(CsvMapWriter csvWriter,
      LinkedList<ValueHolderValueObject> records, String resultType,
      String[] headers, AccountExportValueObject accountExport,
      SearchRecordValueObject document) throws IOException {

    populatePublicContributions(records, document, accountExport);
    populateThumbnails(records, document, accountExport);

    Map<String, Object> csvValues;
    if (EXPORT_TYPE_FIELDS.equals(accountExport.getExportType())) {
      csvValues = getCsvValuesResultFields(records);
    } else {
      csvValues = getCsvValuesResultType(records, resultType, headers);
    }
    csvWriter.write(csvValues, headers);
  }

  private Map<String, Object> getCsvValuesResultFields(
      LinkedList<ValueHolderValueObject> records) {
    Map<String, Object> csvValues = new HashMap<String, Object>();
    for (ValueHolderValueObject record : records) {
      StringBuilder sb = new StringBuilder();
      concatenateValues(record, sb);
      String value = sb.toString().trim();
      if (value.endsWith(characterSeparator)) {
        value = value.substring(0, value.length() - 1);
      }
      value = value.replace("\n", " ");
      csvValues.put(record.getName(), value);
    }
    return csvValues;
  }

  private Map<String, Object> getCsvValuesResultType(
      LinkedList<ValueHolderValueObject> records, String resultType,
      String[] headers) {
    Map<String, Object> csvValues = new HashMap<String, Object>();
    for (String header : headers) {
      if (header.equals(resultType)) {
        csvValues.put(header, getConcatenateValues(records));
      } else {
        csvValues.put(header, null);
      }
    }
    return csvValues;
  }

  private String getConcatenateValues(LinkedList<ValueHolderValueObject> records) {
    StringBuilder sb = new StringBuilder();
    concatenateValues(records, sb);
    String value = sb.toString().trim();
    if (value.endsWith(characterSeparator)) {
      value = value.substring(0, value.length() - 1);
    }
    value = value.replace("\n", " ");
    return value;
  }

  @SuppressWarnings("unchecked")
  private void concatenateValues(LinkedList<ValueHolderValueObject> records,
      StringBuilder sb) {
    for (ValueHolderValueObject record : records) {
    	if (record.getValue() instanceof ArrayList<?>) {
    		try {
	    		for (AspireObject ao : (ArrayList<AspireObject>) record.getValue()) {
						sb.append(ao.getContent("caption") + ": " + ao.getContent("url") + characterSeparator);
	    		}
	    		continue;
    		} catch (AspireException ae) {
				logger.error("An error occurred creating CSV export file. Caused by " + ae);
			}
    		catch (ClassCastException cce) {
			}
    	}
      concatenateValues(record, sb);
    }
  }

  private void concatenateValues(ValueHolderValueObject record, StringBuilder sb) {
    Object value = record.getValue();
    if (value != null) {
      if (value instanceof List) {
        List<?> valueList = (List<?>) value;
        for (Object v : valueList) {
          sb.append(v + characterSeparator);
        }
      } else {
        sb.append(value + characterSeparator);
      }
      return;
    }
    LinkedList<ValueHolderValueObject> fieldValues = record
        .getArrayOfObjectsValue();
    if (fieldValues != null) {
      concatenateValues(fieldValues, sb);
    }

  }

  private String[] getHeaders(AccountExportValueObject accountExport) {

    String[] headers;

    if (EXPORT_TYPE_FIELDS.equals(accountExport.getExportType())) {
      String[] resultFieldsTemp = accountExport.getQueryParameters().get(
          CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME);
      headers = resultFieldsTemp[0].split(",");
      return headers;
    }
    int defaultFieldsIndexSize = DEFAULT_FIELDS_INDEX_MAP.keySet().size();
    headers = DEFAULT_FIELDS_INDEX_MAP.keySet().toArray(
        new String[defaultFieldsIndexSize]);
    return headers;
  }
}
