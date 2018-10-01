package gov.nara.opa.common.services.docstransforms;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.util.LinkedList;

public interface DocumentValuesExtractorService {

  LinkedList<ValueHolderValueObject> getValues(
      SearchRecordValueObject document, AccountExportValueObject accountExport);
}
