package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaNotNullAndNotEmptyForStringConstraintValidator implements
	ConstraintValidator<OpaNotNullAndNotEmpty, String> {

	@Override
	public void initialize(OpaNotNullAndNotEmpty value) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext cxt) {
		return value != null && value.trim().length() > 0;
	}

}
