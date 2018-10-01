package gov.nara.opa.api.validation.constraint.validator;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.validation.constraint.UserNameDoesNotExistAlready;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserNameDoesNotExistAlreadyConstraintValidator implements
    ConstraintValidator<UserNameDoesNotExistAlready, String> {

  @Autowired
  private UserAccountDao administratorUserAccountDao;

  @Override
  public void initialize(UserNameDoesNotExistAlready userName) {
  }

  @Override
  public boolean isValid(String userName, ConstraintValidatorContext cxt) {
    return !administratorUserAccountDao.verifyIfUserNameExists(userName);
  }

}
