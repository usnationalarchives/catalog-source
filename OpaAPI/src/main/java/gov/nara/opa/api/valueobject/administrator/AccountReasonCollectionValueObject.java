package gov.nara.opa.api.valueobject.administrator;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountReasonCollectionValueObject extends
    AbstractWebEntityValueObject implements AccountReasonValueObjectConstants {

  private List<AccountReasonValueObject> accountReasons;
  private int total;

  public static final String ACOUNT_REASON_ENTITY_NAME = "reason";

  public AccountReasonCollectionValueObject(
      List<AccountReasonValueObject> accountReasons) {
    this.accountReasons = accountReasons;
    this.total = accountReasons != null ? accountReasons.size() : 0;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    aspireContent.put(TOTAL_RECORDS_ASP, total);
    aspireContent.put(ACOUNT_REASON_ENTITY_NAME, accountReasons);
    return aspireContent;
  }

}
