package gov.nara.opa.api.controller.system;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.system.UrlMappingService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.system.UrlMappingRequestParameters;
import gov.nara.opa.api.validation.system.UrlMappingValidator;
import gov.nara.opa.api.valueobject.system.UrlMappingCollectionValueObject;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UrlMappingController extends AbstractBaseController {

  @Value("${configFilePath}")
  private String configFilePath;

  @Autowired
  private UrlMappingService urlMappingService;

  @Autowired
  private UrlMappingValidator urlMappingValidator;

  @Autowired
  APIResponse apiResponse;

  public static final String URL_MAPPING_ACTION = "content";

  static Logger log = Logger.getLogger(UrlMappingController.class);

  /**
   * Method to map URLs from OPA Pilot to OPA Prod
   * 
   * @param format
   *          OPA Response Object Format
   * @return Aspire JSON/XML Response Object
   */
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/urlmapping/{recordType}/{arcId}" }, method = RequestMethod.GET)
  public ResponseEntity<String> search(
      @Valid UrlMappingRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {
    ValidationResult validationResult = urlMappingValidator.validate(
        bindingResult, request);

    if (!validationResult.isValid()) {
    	return createErrorResponseEntity(validationResult, request,
    			URL_MAPPING_ACTION);
    }

    UrlMappingCollectionValueObject responseObject = urlMappingService.getUrlMapping(requestParameters);
    
    Integer totalUrlMappings = responseObject.getTotalUrlMappings();
    if (totalUrlMappings.intValue() == 0) {
    	addNoRecordsFoundError(validationResult);
    	return createErrorResponseEntity(validationResult, request,
    			URL_MAPPING_ACTION);
    }

    // Record log entry
    log.info(UsageLogCode.URL_MAPPING + " : "
        + requestParameters.getRecordType() + "/"
        + requestParameters.getArcId());

    return createSuccessResponseEntity(responseObject.getEntityName(), requestParameters, responseObject,
        request, URL_MAPPING_ACTION);
  }
  
  private void addNoRecordsFoundError(ValidationResult validationResult) {
	    ValidationError error = new ValidationError();
	    error.setErrorCode(ErrorCodeConstants.NO_URL_MAPPINGS_FOUND);
	    error.setErrorMessage(ErrorConstants.NO_URL_MAPPINGS_FOUND_MESSAGE);
	    validationResult.addCustomValidationError(error);
	    validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
  }
}
