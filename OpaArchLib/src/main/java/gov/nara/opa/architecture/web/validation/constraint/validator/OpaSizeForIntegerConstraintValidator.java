package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaSize;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaSizeForIntegerConstraintValidator implements
    ConstraintValidator<OpaSize, Integer> {

  private int min;
  private int max;

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext cxt) {
    if (value == null) {
      return true;
    }
    return min <= value && value <= max;
  }

  @Override
  public void initialize(OpaSize constraintAnnotation) {
    min = constraintAnnotation.min();
    max = constraintAnnotation.max();
  }

}
