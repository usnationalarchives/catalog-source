package gov.nara.opa.common.services.docstransforms.impl.doccreators;

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
public class XmlDocumentCreator extends AbstractAspireObjectDocumentCreator
    implements DocumentCreator, Constants {

  @Override
  public void createDocument(LinkedList<ValueHolderValueObject> records,
      OutputStream outputStream, int documentIndex, int totalDocuments,
      boolean flush, String resultType, AccountExportValueObject accountExport,
      Object writer, SearchRecordValueObject document,
      SolrQueryResponseValueObject queryResponse) throws IOException {

    String xmlRecord = "";
    if (records != null) {
      xmlRecord = createDocument(records, outputStream, documentIndex,
          EXPORT_FORMAT_XML, accountExport, document);
    }

    if (documentIndex == 1) {
      xmlRecord = "<naraitems>\n" + xmlRecord;
    }

    if (!xmlRecord.equals("")) {
      xmlRecord = xmlRecord + "\n";
    }

    if (documentIndex == totalDocuments) {
      xmlRecord = xmlRecord + "</naraitems>";
    }

    outputStream.write(xmlRecord.getBytes());
    if (flush) {
      outputStream.flush();
    }
  }
}
