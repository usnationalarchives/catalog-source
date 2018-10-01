package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaUserPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaUserPasswordConstraintValidator implements
    ConstraintValidator<OpaUserPassword, String> {

  int min;

  int max;

  @Override
  public void initialize(OpaUserPassword userPassword) {
    this.min = userPassword.min();
    this.max = userPassword.max();
  }

  @Override
  public boolean isValid(String userPassword, ConstraintValidatorContext cxt) {
    if (userPassword == null) {
      return true;
    }

    return userPassword.length() >= this.min
        && userPassword.length() <= this.max;
  }

}
