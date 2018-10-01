package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.dataaccess.administrator.AccountReasonDao;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;
import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ViewAccountReasonValidator extends AbstractBaseValidator {

  @Autowired
  AccountReasonDao accountReasonDao;

  public static final String ACCOUNT_REASON_OBJECT_KEY = "accountReason";

  public static final String ACCOUNT_REASON_ENTITIES_NAME = "account reasons";

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    List<AccountReasonValueObject> accountReasons = accountReasonDao
        .getAcountReasons(null, null, true);
    if (noRecordsFound(accountReasons, validationResult,
        ErrorCodeConstants.REASONS_NOT_FOUND, ACCOUNT_REASON_ENTITIES_NAME)) {
      return;
    }
    validationResult
        .addContextObject(ACCOUNT_REASON_OBJECT_KEY, accountReasons);
  }

}
