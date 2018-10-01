package gov.nara.opa.api.validation;

import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.MessageInterpolator;

public abstract class OpaApiAbstractValidator extends AbstractBaseValidator {

  @Override
  protected MessageInterpolator getCustomMessageInterpolator() {
    return new OpaApiMessageInterpolator();
  }

}
