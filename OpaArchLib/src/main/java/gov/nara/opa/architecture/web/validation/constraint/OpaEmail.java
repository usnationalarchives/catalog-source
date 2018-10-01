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

@Documented
@Constraint(validatedBy = gov.nara.opa.architecture.web.validation.constraint.validator.OpaEmailConstraintValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OpaEmail {

  String message() default ArchitectureErrorMessageConstants.INVALID_EMAIL;

  String errorCode() default ArchitectureErrorCodeConstants.INVALID_EMAIL;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
