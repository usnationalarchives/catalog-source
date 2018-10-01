package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaSize;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaSizeForStringConstraintValidator implements
    ConstraintValidator<OpaSize, String> {

  private int min;
  private int max;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext cxt) {
    if (value == null) {
      return true;
    }
    return min <= value.length() && value.length() <= max;
  }

  @Override
  public void initialize(OpaSize constraintAnnotation) {
    min = constraintAnnotation.min();
    max = constraintAnnotation.max();
  }

}
