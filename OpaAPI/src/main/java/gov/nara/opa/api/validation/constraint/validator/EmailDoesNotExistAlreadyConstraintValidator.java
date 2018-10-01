package gov.nara.opa.api.validation.constraint.validator;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.validation.constraint.EmailDoesNotExistAlready;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailDoesNotExistAlreadyConstraintValidator implements
	ConstraintValidator<EmailDoesNotExistAlready, String> {

	@Autowired
	private UserAccountDao administratorUserAccountDao;

	@Override
	public void initialize(EmailDoesNotExistAlready email) {
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext cxt) {
		if (email == null) {
			return true;
		}
		return !administratorUserAccountDao.verifyIfEmailExists(email);
	}

}
