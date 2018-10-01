package gov.nara.opa.api.validation;

import gov.nara.opa.api.validation.constraint.messageinterpolator.AccountReasonIdExistsMessageInterpolator;
import gov.nara.opa.api.validation.constraint.messageinterpolator.EmailDoesNotExistAlreadyMessageInterpolator;
import gov.nara.opa.api.validation.constraint.messageinterpolator.UserNameDoesNotExistAlreadyMessageInterpolator;
import gov.nara.opa.api.validation.constraint.messageinterpolator.UserNameExistsMessageInterpolator;
import gov.nara.opa.architecture.web.validation.DefaultErrorMessagesInterpolator;

/**
 * Adds a custom message interpolator to the default list of
 * message interpolators used by thhe DefaultErrorMessagesInterpolator
 */
public class OpaApiMessageInterpolator extends DefaultErrorMessagesInterpolator {

  static {
    messageInterpolators.put("EmailDoesNotExistAlready",
        new EmailDoesNotExistAlreadyMessageInterpolator());
    messageInterpolators.put("UserNameDoesNotExistAlready",
        new UserNameDoesNotExistAlreadyMessageInterpolator());
    messageInterpolators.put("UserNameExists",
        new UserNameExistsMessageInterpolator());
    messageInterpolators.put("AccountReasonIdExists",
        new AccountReasonIdExistsMessageInterpolator());
  }

}
