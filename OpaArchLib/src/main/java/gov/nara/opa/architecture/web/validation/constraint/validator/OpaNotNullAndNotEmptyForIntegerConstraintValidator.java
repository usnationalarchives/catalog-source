package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaNotNullAndNotEmptyForIntegerConstraintValidator implements
    ConstraintValidator<OpaNotNullAndNotEmpty, Integer> {

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext cxt) {
    return value != null;
  }

  @Override
  public void initialize(OpaNotNullAndNotEmpty constraintAnnotation) {

  }

}
