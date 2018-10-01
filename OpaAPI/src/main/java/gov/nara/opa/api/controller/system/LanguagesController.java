package gov.nara.opa.api.controller.system;

import gov.nara.opa.api.services.system.LanguageService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.system.LanguageRequestParameters;
import gov.nara.opa.api.validation.system.LanguagesValidator;
import gov.nara.opa.api.valueobject.system.LanguageCollectionValueObject;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.AnnotationConstants;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@Controller
public class LanguagesController extends AbstractBaseController {

	@Autowired
	private LanguageService languageService;

	@Autowired
	private LanguagesValidator languagesValidator;

	public static final String LANGUAGES_ACTION = "content";

	/**
	   * Retrieves all languages supported by NARA Catalog
	   * @return
	   */
//	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM + "/languages" },  
//		      method = RequestMethod.GET)
//	public ResponseEntity<String> getLanguages(
//			@Valid LanguageRequestParameters requestParameters,
//			BindingResult bindingResult, HttpServletRequest request ) {
//
//		LanguageCollectionValueObject responseObject = languageService.getLanguages();
//
//	    Integer totalLanguages = responseObject.getTotalLanguages();
//
//	    ValidationResult validationResult = languagesValidator.validate(bindingResult, request);
//	    if (totalLanguages.intValue() == 0) {
//	    	addNoRecordsFoundError(validationResult);
//	    	return createErrorResponseEntity(validationResult, request,
//	    			LANGUAGES_ACTION);
//	    }
//
//		return createSuccessResponseEntity(AnnotationConstants.LANGUAGES,
//				requestParameters, responseObject, request, LANGUAGES_ACTION);
//	}

	private void addNoRecordsFoundError(ValidationResult validationResult) {
		ValidationError error = new ValidationError();
		error.setErrorCode(ErrorCodeConstants.LANGUAGES_NOT_FOUND);
		error.setErrorMessage(ErrorConstants.NO_LANGUAGES_FOUND_MESSAGE);
		validationResult.addCustomValidationError(error);
		validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
	}
}
