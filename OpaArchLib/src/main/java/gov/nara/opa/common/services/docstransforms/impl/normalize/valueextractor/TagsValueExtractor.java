package gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor;

import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import org.springframework.stereotype.Component;

@Component
public class TagsValueExtractor extends AbstractValueExtractor {

  @Override
  protected Object getValue(String valueGenerationInstruction,
      SearchRecordValueObject document, String type,
      AccountExportValueObject accountExport, String section) {
    if (!accountExport.getIncludeTags().booleanValue()) {
      return null;
    }
    return null;
  }

}
