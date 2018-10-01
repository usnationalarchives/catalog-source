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
@Constraint(validatedBy = {
    gov.nara.opa.architecture.web.validation.constraint.validator.OpaSizeForStringConstraintValidator.class,
    gov.nara.opa.architecture.web.validation.constraint.validator.OpaSizeForIntegerConstraintValidator.class })
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OpaSize {

  int min() default 0;

  int max() default Integer.MAX_VALUE;

  String message() default ArchitectureErrorMessageConstants.EXCEEDS_SIZE;

  String errorCode() default ArchitectureErrorCodeConstants.EXCEEDS_SIZE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
