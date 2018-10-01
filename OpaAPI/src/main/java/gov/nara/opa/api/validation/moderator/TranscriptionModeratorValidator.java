package gov.nara.opa.api.validation.moderator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.storage.OpaStorageFactory;

@Component
public class TranscriptionModeratorValidator extends OpaApiAbstractValidator {

  @Autowired
  private PageNumberUtils pageNumberUtils;

  @Autowired
  private OpaStorageFactory opaStorageFactory;
  
  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    // TODO Auto-generated method stub

  }

}
