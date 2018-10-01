package gov.nara.opa.common.valueobject.export;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountExportCollectionValueObject extends
    AbstractWebEntityValueObject {

  private List<AccountExportValueObject> accountExports;
  private int total;

  public AccountExportCollectionValueObject(
      List<AccountExportValueObject> accountExports) {
    this.accountExports = accountExports;
    this.total = accountExports.size();
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

    aspireContent.put("@total", total);
    aspireContent.put("accountExport", accountExports);
    return aspireContent;
  }

}
