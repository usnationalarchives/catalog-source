package gov.nara.opa.architecture.web.validation.constraint;

import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author aolaru
 * @date Jun 5, 2014
 * 
 */
@Documented
@Constraint(validatedBy = { gov.nara.opa.architecture.web.validation.constraint.validator.OpaConstrainedStringListConstraintValidator.class })
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OpaConstrainedStringList {

  String[] allowedValues();

  String message() default ArchitectureErrorMessageConstants.IS_NOT_CONTAINED_IN_LIST;

  String errorCode() default ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
