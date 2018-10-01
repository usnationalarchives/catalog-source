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
@Constraint(validatedBy = gov.nara.opa.architecture.web.validation.constraint.validator.OpaUserPasswordConstraintValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OpaUserPassword {

  int min() default 8;

  int max() default 32;

  String errorCode() default ArchitectureErrorCodeConstants.BAD_PASSWORD;

  String message() default ArchitectureErrorMessageConstants.INVALID_PASSWORD;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
