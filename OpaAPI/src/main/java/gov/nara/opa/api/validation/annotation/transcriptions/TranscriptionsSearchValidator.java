package gov.nara.opa.api.validation.annotation.transcriptions;

import gov.nara.opa.api.services.search.SingleSearchRecordRetrieval;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TranscriptionsSearchValidator extends OpaApiAbstractValidator {

  @Override
  public void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {

  }

  @Autowired
  SingleSearchRecordRetrieval searchRecordRetrieval;

}
