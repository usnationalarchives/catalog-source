package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

public interface DocumentCreator {
  public void createDocument(LinkedList<ValueHolderValueObject> records,
      OutputStream outputStream, int documentIndex, int totalDocuments,
      boolean flush, String resultType, AccountExportValueObject accountExport,
      Object writer, SearchRecordValueObject document,
      SolrQueryResponseValueObject queryResponse) throws IOException;
}
