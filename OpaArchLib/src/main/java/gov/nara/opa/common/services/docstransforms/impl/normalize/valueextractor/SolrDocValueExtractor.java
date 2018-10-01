package gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor;

import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import org.apache.solr.common.SolrDocument;

public class SolrDocValueExtractor extends AbstractValueExtractor {

  @Override
  protected Object getValue(String valueGenerationInstruction,
      SearchRecordValueObject document, String type,
      AccountExportValueObject accountExport, String section) {

    SolrDocument solrDocument = document.getSolrDocument();
    Object value = solrDocument.get(valueGenerationInstruction);
    return getCastedObjectValue(value, type);
  }

}
