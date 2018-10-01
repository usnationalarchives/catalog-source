package gov.nara.opa.architecture.web.validation.constraint.validator;

import gov.nara.opa.architecture.web.validation.constraint.OpaEmail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpaEmailConstraintValidator implements
	ConstraintValidator<OpaEmail, String> {

	private Pattern pattern;
	private Matcher matcher;

	private static final String EMAIL_PATTERN = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+[A-Za-z]{2,6}$";

	@Override
	public void initialize(OpaEmail email) {
		pattern = Pattern.compile(EMAIL_PATTERN);
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext cxt) {
		if (email == null) {
			return true;
		}

		matcher = pattern.matcher(email);
		return matcher.matches();
	}

}
