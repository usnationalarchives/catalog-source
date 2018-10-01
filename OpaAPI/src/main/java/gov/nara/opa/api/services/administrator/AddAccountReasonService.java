package gov.nara.opa.api.services.administrator;

import gov.nara.opa.api.validation.administrator.AddAccountReasonRequestParameters;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;

public interface AddAccountReasonService {

  AccountReasonValueObject create(
      AddAccountReasonRequestParameters requestParameters);
}
