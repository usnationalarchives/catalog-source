package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaConstrainedStringList;

import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaConstrainedStringListConstraintValidator implements
    ConstraintValidator<OpaConstrainedStringList, List<?>> {

  List<?> allowedValues;

  @Override
  public boolean isValid(List<?> values, ConstraintValidatorContext cxt) {
    if (values == null) {
      return true;
    }
    if (allowedValues != null && allowedValues.size() > 0) {
      for (Object value : values) {
        if (!allowedValues.contains(value)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void initialize(OpaConstrainedStringList constraintAnnotation) {
    String[] values = constraintAnnotation.allowedValues();
    if (values != null && values.length > 0) {
      allowedValues = Arrays.asList(constraintAnnotation.allowedValues());
    }
  }
}
