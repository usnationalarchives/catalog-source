package gov.nara.opa.api.valueobject.user.logs;

import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

public class AccountLogValueObjectHelper {

  public static AccountLogValueObject createAccountLogForInsert(
      UserAccountValueObject userAccount, String action,
      Integer adminAccountId, Integer reasonId, String notes) {
    AccountLogValueObject log = new AccountLogValueObject();
    log.setAccountId(userAccount.getAccountId());
    log.setAction(action);
    log.setAdminAccountId(adminAccountId);
    log.setLogTs(TimestampUtils.getUtcTimestamp());
    log.setStatus(true);
    log.setReasonId(reasonId);
    log.setNotes(notes);
    return log;
  }

  public static AccountLogValueObject createAccountLogForInsert(
      UserAccountValueObject userAccount, String action) {
    return createAccountLogForInsert(userAccount, action, null, null, null);
  }

  public static AccountLogValueObject createAccountLogForInsert(
      UserAccountValueObject userAccount, String action, Integer adminAccountId) {
    return createAccountLogForInsert(userAccount, action, adminAccountId, null,
        null);
  }

}
