package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JsonPublicAPIDocumentCreator extends
    AbstractAspireObjectDocumentCreator implements DocumentCreator, Constants {

  @Override
  public void createDocument(LinkedList<ValueHolderValueObject> records,
      OutputStream outputStream, int documentIndex, int totalDocuments,
      boolean flush, String resultType, AccountExportValueObject accountExport,
      Object writer, SearchRecordValueObject document,
      SolrQueryResponseValueObject queryResponse) throws IOException {

    if (records == null && accountExport.extractValuesNeeded()) {
      if (documentIndex == 1) {
        outputStream.write(getJsonBeginning(accountExport, queryResponse)
            .getBytes());
      }
      if (documentIndex == totalDocuments) {
        outputStream.write(getJsonEnding(accountExport, "").getBytes());
        if (flush) {
          outputStream.flush();
        }
      }
      return;
    }

    String jsonRecord = "";
    try {
      AspireObject aspireObject = createAspireObjectPublicSearchResult(
          document, accountExport, documentIndex);
      if (aspireObject != null && aspireObject.getChildren() != null
          && aspireObject.getChildren().size() > 0) {
        jsonRecord = getJsonRecord(aspireObject, accountExport);
      } else {
        if (accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)) {
          jsonRecord = "{}";
        } else {
          jsonRecord = "      {}";
        }
      }

    } catch (AspireException e) {
      throw new OpaRuntimeException(e);
    }

    if (documentIndex == 1) {
      jsonRecord = getJsonBeginning(accountExport, queryResponse) + jsonRecord;
    } else {
      if (accountExport.getRecordsWritten().get() > 0) {
        if (accountExport.getPrintingFormat().equals(
            PRINTING_FORMAT_PRETTY_FALSE)) {
          jsonRecord = "," + jsonRecord;
        } else {
          if (accountExport.getIncludeOpaResponseWrapper()) {
            if (jsonRecord.endsWith("{}")) {
              jsonRecord = ",\n" + jsonRecord;
            } else {
              jsonRecord = "      ,\n" + jsonRecord;
            }
          } else {
            jsonRecord = "  ,\n" + jsonRecord;
          }

        }
      }

    }
    if (documentIndex == totalDocuments) {
      jsonRecord = jsonRecord + getJsonEnding(accountExport, jsonRecord);
    }
    accountExport.getRecordsWritten().incrementAndGet();
    outputStream.write(jsonRecord.getBytes());
    if (flush) {
      outputStream.flush();
    }
  }
}
