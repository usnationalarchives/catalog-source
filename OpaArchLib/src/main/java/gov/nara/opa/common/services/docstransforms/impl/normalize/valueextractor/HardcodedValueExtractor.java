package gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor;

import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

public class HardcodedValueExtractor extends AbstractValueExtractor {

  @Override
  protected Object getValue(String valueGenerationInstruction,
      SearchRecordValueObject document, String type,
      AccountExportValueObject accountExport, String section) {

    return getCastedObjectValue(valueGenerationInstruction, type);
  }

}
