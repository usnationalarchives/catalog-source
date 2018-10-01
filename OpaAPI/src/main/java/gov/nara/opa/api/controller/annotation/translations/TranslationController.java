package gov.nara.opa.api.controller.annotation.translations;

import gov.nara.opa.api.constants.AnnotationsConstants;
import gov.nara.opa.api.services.annotation.locks.AnnotationLockService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.annotation.locks.AnnotationLockLanguageRequestParameters;
import gov.nara.opa.api.validation.annotation.locks.AnnotationLockValidator;
import gov.nara.opa.api.validation.annotation.translations.TranslationsSaveRequestParameters;
import gov.nara.opa.api.validation.annotation.translations.TranslationsSaveValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObject;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@Controller
public class TranslationController extends AbstractBaseController {

	private static OpaLogger logger = OpaLogger.getLogger(TranslationController.class);

	@Autowired
	private AnnotationLockValidator locksValidator;

	@Autowired
	private AnnotationLockService lockService;

	@Autowired
	private TranslationsSaveValidator saveTranslationsValidator;

	/**
	   * Processes all translations lock operations
	   * 
	   * @param webRequest
	   * @param apiType
	   * @param naId
	   * @param objectId
	   * @param action
	   *          Allowed values are lock
	   * @param text
	   * @param format
	   * @param pretty
	   * @return
	   */
//	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM +
//			"/id/{naId}/objects/{objectId:.+}/translations" }, 
//		      params = "action=" + AnnotationsConstants.LOCK_ACTION,
//		      method = RequestMethod.PUT)
//	public ResponseEntity<String> createLock (
//			@Valid AnnotationLockLanguageRequestParameters requestParameters,
//			BindingResult bindingResult, HttpServletRequest request) {
//
//		logger.info("Entered the lock method with http parameters: "
//            + request.getParameterMap());
//		ValidationResult validationResult = locksValidator.validate(
//				bindingResult, request);
//		if (!validationResult.isValid()) {
//			return createErrorResponseEntity(validationResult, request,
//					AnnotationsConstants.LOCK_ACTION);
//		}
//		requestParameters.setHttpSessionId(request.getSession().getId());
//		AnnotationLockValueObject lock = lockService.create(requestParameters);
//		logger.info(String.format("naId=%1$s, objectId=%2$s, message=Locked translation", 
//				requestParameters.getNaId(), requestParameters.getObjectId()));
//		
//		return createSuccessResponseEntity(AnnotationConstants.TRANSLATIONS,
//				requestParameters, lock, request, AnnotationsConstants.LOCK_ACTION);
//	}

	/**
	   * Processes all translations unlock operations
	   * 
	   * @param webRequest
	   * @param apiType
	   * @param naId
	   * @param objectId
	   * @param action
	   *          Allowed values are unlock
	   * @param text
	   * @param format
	   * @param pretty
	   * @return
	   */
//	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM +
//			"/id/{naId}/objects/{objectId:.+}/translations" }, 
//		      params = "action=" + AnnotationsConstants.UNLOCK_ACTION,
//		      method = RequestMethod.PUT)
//	public ResponseEntity<String> removeLock (
//			@Valid AnnotationLockLanguageRequestParameters requestParameters,
//			BindingResult bindingResult, HttpServletRequest request) {
//
//		logger.info("Entered the unlock method with http parameters: "
//          + request.getParameterMap());
//		ValidationResult validationResult = locksValidator.validate(
//				bindingResult, request);
//		if (!validationResult.isValid()) {
//			return createErrorResponseEntity(validationResult, request,
//					AnnotationsConstants.LOCK_ACTION);
//		}
//		requestParameters.setHttpSessionId(request.getSession().getId());
//		lockService.delete(requestParameters);
//		return createSuccessResponseEntity(AnnotationConstants.TRANSLATIONS,
//				requestParameters, null, request, AnnotationsConstants.LOCK_ACTION);
//	}

	/**
	   * Processes all translations save operations
	   * 
	   * @param webRequest
	   * @param apiType
	   * @param naId
	   * @param objectId
	   * @param action
	   *          Allowed values are lock, unlock
	   * @param text
	   * @param format
	   * @param pretty
	   * @return
	   */
//	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM +
//			"/id/{naId}/objects/{objectId:.+}/translations" }, 
//		      method = RequestMethod.PUT)
//	public ResponseEntity<String> saveTranslation (
//			@Valid TranslationsSaveRequestParameters requestParameters,
//			BindingResult bindingResult, HttpServletRequest request) {
//
//		logger.info("Entered the save translation method with http parameters: "
//          + request.getParameterMap());
//		ValidationResult validationResult = saveTranslationsValidator.validate(
//				bindingResult, request);
//		if (!validationResult.isValid()) {
//			return createErrorResponseEntity(validationResult, request,
//					AnnotationConstants.TRANSLATIONS);
//		}
//		requestParameters.setHttpSessionId(request.getSession().getId());
//		//AnnotationLockValueObject lock = lockService.create(requestParameters);
//		return createSuccessResponseEntity(AnnotationConstants.TRANSLATIONS,
//				requestParameters, null, request, AnnotationConstants.TRANSLATIONS);
//	}
}