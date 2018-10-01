package gov.nara.opa.api.valueobject.administrator;

import gov.nara.opa.api.validation.administrator.AddAccountReasonRequestParameters;
import gov.nara.opa.architecture.utils.TimestampUtils;

public class AccountReasonValueObjectHelper {

  public static AccountReasonValueObject createAccountResonForInsert(
      AddAccountReasonRequestParameters requestParameters, Integer accountId) {
    AccountReasonValueObject accountReason = new AccountReasonValueObject();
    accountReason.setReason(requestParameters.getText());
    accountReason.setAccountId(accountId);
    accountReason.setReasonAddedTs(TimestampUtils.getTimestamp());
    accountReason.setReasonStatus(true);
    return accountReason;
  }
}
