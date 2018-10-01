package gov.nara.opa.common.services.docstransforms;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.OutputStream;
import java.util.LinkedList;

public interface DocumentCreatorService {

  void createDocument(LinkedList<ValueHolderValueObject> records,
      AccountExportValueObject accountExport, OutputStream outputStream,
      int documentIndex, int totalDocuments, boolean flush, String resultType,
      Object writer, SearchRecordValueObject document,
      SolrQueryResponseValueObject queryResponse);
}
