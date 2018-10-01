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
public class XmlPublicAPIDocumentCreator extends
    AbstractAspireObjectDocumentCreator implements DocumentCreator, Constants {

  @Override
  public void createDocument(LinkedList<ValueHolderValueObject> records,
      OutputStream outputStream, int documentIndex, int totalDocuments,
      boolean flush, String resultType, AccountExportValueObject accountExport,
      Object writer, SearchRecordValueObject document,
      SolrQueryResponseValueObject queryResponse) throws IOException {

    String xmlRecord = "";

    if (document != null) {
      try {
        AspireObject aspireObject = createAspireObjectPublicSearchResult(
            document, accountExport, documentIndex);
        if (aspireObject != null) {
          xmlRecord = getXmlRecord(aspireObject, accountExport);
        }
      } catch (AspireException e) {
        throw new OpaRuntimeException(e);
      }
    }

    if (documentIndex == 1) {
      xmlRecord = getXmlBeginning(accountExport, queryResponse) + xmlRecord;
    } else {
      if (accountExport.getPrintingFormat().equals(PRINTING_RECORD_LINE)) {
        xmlRecord = "\n" + xmlRecord;
      }
    }

    if (documentIndex == totalDocuments) {
      xmlRecord = xmlRecord + getXmlEnding(accountExport);
    }

    outputStream.write(xmlRecord.getBytes());
    if (flush) {
      outputStream.flush();
    }
  }
}
