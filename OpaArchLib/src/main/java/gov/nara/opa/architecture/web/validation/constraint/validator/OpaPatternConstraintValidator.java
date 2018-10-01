package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaPatternConstraintValidator implements
    ConstraintValidator<OpaPattern, CharSequence> {

  private java.util.regex.Pattern pattern;

  public void initialize(OpaPattern parameters) {
    OpaPattern.Flag[] flags = parameters.flags();
    int intFlag = 0;
    for (OpaPattern.Flag flag : flags) {
      intFlag = intFlag | flag.getValue();
    }

    try {
      pattern = java.util.regex.Pattern.compile(parameters.regexp(), intFlag);
    } catch (PatternSyntaxException e) {
      throw new OpaRuntimeException(e);
    }
  }

  public boolean isValid(CharSequence value,
      ConstraintValidatorContext constraintValidatorContext) {
    if (value == null) {
      return true;
    }
    Matcher m = pattern.matcher(value);
    return m.matches();
  }
}
