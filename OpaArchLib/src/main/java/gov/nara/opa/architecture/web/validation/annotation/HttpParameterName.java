package gov.nara.opa.architecture.web.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface HttpParameterName {

  /**
   * The name of the request parameter to bind to.
   */
  String value();

}
