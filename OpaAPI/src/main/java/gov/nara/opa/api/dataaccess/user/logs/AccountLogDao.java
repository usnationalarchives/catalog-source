package gov.nara.opa.api.dataaccess.user.logs;

import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObject;

import java.util.List;

public interface AccountLogDao {

  void create(AccountLogValueObject log);

  List<AccountLogValueObject> getAccountNotes(String userName);

}
