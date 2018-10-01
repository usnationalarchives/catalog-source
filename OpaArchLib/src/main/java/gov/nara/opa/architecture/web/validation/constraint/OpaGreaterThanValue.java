package gov.nara.opa.architecture.web.validation.constraint;

import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = {
		gov.nara.opa.architecture.web.validation.constraint.validator.OpaGreaterThanValueForIntegerConstraintValidator.class
})
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OpaGreaterThanValue {

	  int min() default 0;

	  String message() default ArchitectureErrorMessageConstants.EXCEEDS_VALUE_MIN_INT;

	  String errorCode() default ArchitectureErrorCodeConstants.INVALID_PATTERN;

	  Class<?>[] groups() default {};

	  Class<? extends Payload>[] payload() default {};
}
