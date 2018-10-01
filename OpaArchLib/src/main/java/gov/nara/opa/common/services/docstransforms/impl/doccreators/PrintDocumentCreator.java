package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PrintDocumentCreator extends AbstractAspireObjectDocumentCreator
    implements DocumentCreator, Constants {

  @Override
  public void createDocument(LinkedList<ValueHolderValueObject> records,
    OutputStream outputStream, int documentIndex, int totalDocuments,
    boolean flush, String resultType, AccountExportValueObject accountExport,
    Object writer, SearchRecordValueObject document,
    SolrQueryResponseValueObject queryResponse) throws IOException {

    if (records == null) {
      if (documentIndex == 1) {
        outputStream.write("[\n".getBytes());
      }
      if (documentIndex == totalDocuments) {
        outputStream.write("\n]".getBytes());
        if (flush) {
          outputStream.flush();
        }
      }
      return;
    }
    records = flattenValue(records);
    populatePublicContributions(records, document, accountExport);
    populateThumbnails(records, document, accountExport);

    String printRecord = null;
    try {
      printRecord = getJsonRecord(records, document);
    } catch (AspireException e) {
      new OpaRuntimeException(e);
    }
    printRecord = printRecord.replaceAll("\\{.*\"naraitem\"\\:", "");
    printRecord = printRecord.substring(0, printRecord.length() - 1);
    if (documentIndex == 1) {
      printRecord = "[\n" + printRecord;
    } else {
      if (accountExport.getRecordsWritten().get() > 0) {
        printRecord = ",\n" + printRecord;
      }

    }
    if (documentIndex == totalDocuments) {
      printRecord = printRecord + "\n]";
    }
    accountExport.getRecordsWritten().incrementAndGet();
    outputStream.write(printRecord.getBytes());
    if (flush) {
      outputStream.flush();
    }
  }

  @SuppressWarnings("resource")
  private String getJsonRecord(LinkedList<ValueHolderValueObject> records,
      SearchRecordValueObject document) throws AspireException {
    AspireObject ao = new AspireObject("naraitem");

    Map<String, String> extractedFields = new HashMap<String, String>();
    List<AspireObject> fieldsAspireObjects = new ArrayList<AspireObject>();
    for (ValueHolderValueObject record : records) {
      Map<String, String> extractedField = addValueToAspireObject(
          fieldsAspireObjects, record);
      if (extractedField != null) {
        extractedFields.putAll(extractedField);
      }
    }
    String naId = document.getNaId();
    if (naId != null) {
      AspireObject naIdAspireObject = new AspireObject("naId", naId);
      ao.add(naIdAspireObject);
    }

    for (String labelExtractedField : extractedFields.keySet()) {
      AspireObject extractedFielAspireObject = new AspireObject(
          labelExtractedField, extractedFields.get(labelExtractedField));
      ao.add(extractedFielAspireObject);
    }
    ao.add(fieldsAspireObjects);
    return ao.toJsonString();
  }

  private Map<String, String> addValueToAspireObject(
      List<AspireObject> aspireObjects, ValueHolderValueObject value)
      throws AspireException {
		String title = getDocumentTitle(value);
		if (title != null) {
		  Map<String, String> extractedField = new HashMap<String, String>();
		  extractedField.put("documentTitle", title);
		  if (value.getName().equals("geographicSubject") ||
			  value.getName().equals("topicalSubject")) {
			  addAspireObject(aspireObjects, value);
		  }
		  return extractedField;
		}
		if (value.getName() != null && value.getName().equals("date")) {
		  Map<String, String> extractedField = new HashMap<String, String>();
		  extractedField.put("date", (String) value.getValue());
		  addAspireObject(aspireObjects, value);
		  return extractedField;
		}
		
		addAspireObject(aspireObjects, value);
		return null;
  }
	  
  private void addAspireObject (List<AspireObject> aspireObjects, 
		  ValueHolderValueObject value) throws AspireException {
	  if (value.getValue() != null) {
		AspireObject fieldsAspireObject = new AspireObject("fields");
		fieldsAspireObject.add("label", value.getLabel());
		fieldsAspireObject.add("value", value.getValue());
		if (value.getSection() != null) {
		  fieldsAspireObject.add("section", value.getSection());
		}
		aspireObjects.add(fieldsAspireObject);
	  }
  }

  private String getDocumentTitle(ValueHolderValueObject value) {
    if (value == null || value.getName() == null) {
      return null;
    }

    if (value.getName().equals("title") || value.getName().equals("personName")
        || value.getName().equals("organizationName")
        || value.getName().equals("topicalSubject")
        || value.getName().equals("geographicSubject")
        || value.getName().equals("specificRecordsType")) {
      return (String) value.getValue();
    }
    return null;
  }
}
