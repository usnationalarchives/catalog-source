package gov.nara.opa.api.dataaccess.administrator;

import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;

import java.util.List;

public interface AccountReasonDao {

  void create(AccountReasonValueObject accountReason);

  List<AccountReasonValueObject> getAcountReasons(Integer accountId,
      String reason, Boolean reasonStatus);

  AccountReasonValueObject getAcountReason(Integer reasonId);

}
