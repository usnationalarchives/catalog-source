package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaNotNullAndNotEmptyForListConstraintValidator implements
    ConstraintValidator<OpaNotNullAndNotEmpty, List<?>> {

  @Override
  public boolean isValid(List<?> value, ConstraintValidatorContext cxt) {
    boolean returnValue = true;
    if (value == null || value.size() == 0) {
      returnValue = false;
    }
    return returnValue;
  }

  @Override
  public void initialize(OpaNotNullAndNotEmpty constraintAnnotation) {

  }

}
