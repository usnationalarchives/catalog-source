package gov.nara.opa.api.validation.constraint.validator;

import gov.nara.opa.api.dataaccess.administrator.AccountReasonDao;
import gov.nara.opa.api.validation.constraint.AccountReasonIdExists;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountReasonIdExistsConstraintValidator implements
    ConstraintValidator<AccountReasonIdExists, Integer> {

  @Autowired
  private AccountReasonDao accountReasonDao;

  @Override
  public void initialize(AccountReasonIdExists reasonId) {
  }

  @Override
  public boolean isValid(Integer reasonId, ConstraintValidatorContext cxt) {
    if (reasonId == null) {
      return false;
    }
    AccountReasonValueObject accountReason = accountReasonDao
        .getAcountReason(reasonId);
    if (accountReason == null || !accountReason.getReasonStatus()) {
      return false;
    }
    return true;
  }

}
