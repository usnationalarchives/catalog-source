package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaGreaterThanValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaGreaterThanValueForIntegerConstraintValidator implements
		ConstraintValidator<OpaGreaterThanValue, Integer> {

  private int min;
  
	@Override
	public void initialize(OpaGreaterThanValue constraintAnnotation) {
    min = constraintAnnotation.min();
	}

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
	  if(value == null) {
	    return true;
	  }
	  
		return value > min;
	}

}
