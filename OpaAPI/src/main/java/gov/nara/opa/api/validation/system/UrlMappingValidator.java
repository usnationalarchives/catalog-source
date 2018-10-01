package gov.nara.opa.api.validation.system;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.ResultTypeConstants;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;


@Component
public class UrlMappingValidator extends OpaApiAbstractValidator implements ResultTypeConstants {

  public static final String URL_MAPPING_FIELD_NAME = "recordType";

  @Override
  public void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    UrlMappingRequestParameters requestParameters = (UrlMappingRequestParameters) validationResult
        .getValidatedRequest();
    validateRecordType(requestParameters.getRecordType(), validationResult);
  }

  private boolean validateRecordType(String recordType,
      ValidationResult validationResult) {
    if (!recordType.equals(ResultTypeConstants.RESULT_TYPE_ORGANIZATION) 
    	&& !recordType.equals(ResultTypeConstants.RESULT_TYPE_PERSON)
        && !recordType.equals(ResultTypeConstants.RESULT_TYPE_SPECIFIC_RECORDS_TYPE)
        && !recordType.equals(ResultTypeConstants.RESULT_TYPE_TOPICAL_SUBJECT)
        && !recordType.equals(ResultTypeConstants.RESULT_TYPE_GEOGRAPHIC_REFERENCE)
        && !recordType.equals(ResultTypeConstants.RESULT_TYPE_ALL) ) {
	      ValidationError validationError = new ValidationError();
	      validationError
	          .setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
	      validationError.setErrorMessage(ErrorConstants.INVALID_RECORD_TYPE);
	      validationError.setFieldValidationError(true);
	      validationError.setValidatedItemCode(URL_MAPPING_FIELD_NAME);
	      validationResult.addCustomValidationError(validationError);
	      return false;
    }
    return true;
  }

}