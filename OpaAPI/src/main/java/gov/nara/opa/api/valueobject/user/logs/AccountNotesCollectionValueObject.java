package gov.nara.opa.api.valueobject.user.logs;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountNotesCollectionValueObject extends
    AbstractWebEntityValueObject implements UserAccountValueObjectConstants {

  private String userName;
  private List<AccountLogValueObject> notes;
  private int total;

  public static final String ACCOUNT_NOTE_ENTITY_NAME = "note";

  public AccountNotesCollectionValueObject(List<AccountLogValueObject> notes,
      String userName) {
    this.notes = notes;
    this.userName = userName;
    this.total = notes == null ? 0 : notes.size();
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    aspireContent.put(TOTAL_RECORDS_ASP, total);
    aspireContent.put(USER_NAME_ASP, userName);
    aspireContent.put(ACCOUNT_NOTE_ENTITY_NAME, notes);
    return aspireContent;
  }

}
