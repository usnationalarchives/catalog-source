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
@Constraint(validatedBy = gov.nara.opa.api.validation.constraint.validator.AccountReasonIdExistsConstraintValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountReasonIdExists {

  String message() default ErrorConstants.ACCOUNT_REASON_ID_DOES_NOT_EXIST;

  String errorCode() default OpaErrorCodeConstants.ACCOUNT_REASON_ID_DOES_NOT_EXIST;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
