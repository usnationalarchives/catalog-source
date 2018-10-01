package gov.nara.opa.common.services.docstransforms;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.OutputStream;

public interface DocumentTransformerService {

  void transformDocument(SearchRecordValueObject document,
      AccountExportValueObject accountExport, OutputStream outputStream,
      int documentIndex, int totalDocuments, boolean flush, Object writer,
      SolrQueryResponseValueObject queryResponse);
}
