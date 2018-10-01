package gov.nara.opa.api.validation.constraint;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.system.OpaErrorCodeConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = gov.nara.opa.api.validation.constraint.validator.EmailDoesNotExistAlreadyConstraintValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailDoesNotExistAlready {

  String message() default ErrorConstants.USER_EMAIL_ALREADY_EXISTS;

  String errorCode() default OpaErrorCodeConstants.EMAIL_EXISTS;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
