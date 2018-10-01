package gov.nara.opa.api.controller.moderator;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import gov.nara.opa.api.services.moderator.BackgroundImageService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.moderator.BackgroundImageValidator;
import gov.nara.opa.api.validation.moderator.CreateDeleteBackgroundImageRequestParameters;
import gov.nara.opa.api.validation.moderator.ViewBackgroundImageRequestParameters;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageCollectionValueObject;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObject;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObjectConstants;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class BackgroundImageController extends AbstractBaseController {

	@Autowired
	private BackgroundImageService backgroundImageService;

	@Autowired
	private BackgroundImageValidator backgroundImageValidator;

	public static final String BACKGROUND_IMAGE_PARENT_ENTITY_NAME = "background-image";
	public static final String BACKGROUND_IMAGES_PARENT_ENTITY_NAME = "background-images";

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/background-image",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/background-image" }, method = RequestMethod.POST)
	public ResponseEntity<String> addBackgroundImage(
			@Valid CreateDeleteBackgroundImageRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		ValidationResult validationResult = backgroundImageValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					BackgroundImageValueObjectConstants.ADD_BACKGROUND_IMAGE);
		}

		BackgroundImageValueObject backgroundImage = backgroundImageService
				.addBackgroundImage(requestParameters);

		if (backgroundImage == null) {
			return createErrorResponseEntity(
					ErrorConstants.BACKGROUND_IMAGE_NOT_FOUND,
					ErrorCodeConstants.BACKGROUND_IMAGE_NOT_FOUND,
					requestParameters, HttpStatus.NOT_FOUND, request,
					BackgroundImageValueObjectConstants.ADD_BACKGROUND_IMAGE);
		}

		return createSuccessResponseEntity(BACKGROUND_IMAGE_PARENT_ENTITY_NAME,
				requestParameters, backgroundImage, request,
				BackgroundImageValueObjectConstants.ADD_BACKGROUND_IMAGE);
	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/background-image",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/background-image" }, method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteBackgroundImage(
			@Valid CreateDeleteBackgroundImageRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		BackgroundImageValueObject backgroundImage = backgroundImageService
				.deleteBackgroundImage(requestParameters);

		if (backgroundImage == null) {
			return createErrorResponseEntity(
					ErrorConstants.BACKGROUND_IMAGE_NOT_FOUND,
					ErrorCodeConstants.BACKGROUND_IMAGE_NOT_FOUND,
					requestParameters, HttpStatus.NOT_FOUND, request,
					BackgroundImageValueObjectConstants.DELETE_BACKGROUND_IMAGE);
		}

		return createSuccessResponseEntity(BACKGROUND_IMAGE_PARENT_ENTITY_NAME,
				requestParameters, backgroundImage, request,
				BackgroundImageValueObjectConstants.DELETE_BACKGROUND_IMAGE);

	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/background-image",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/background-image" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewBackgroundImagesForModerator(
			@Valid ViewBackgroundImageRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		BackgroundImageCollectionValueObject backgroundImages = backgroundImageService
				.getAllBackgroundImages();

		AbstractWebEntityValueObject responseObject = null;
		String entityName = null;

		if (backgroundImages == null
				|| backgroundImages.getBackgroundImages().size() == 0) {
			return createErrorResponseEntity(
					ErrorConstants.BACKGROUND_IMAGE_NOT_FOUND,
					ErrorCodeConstants.BACKGROUND_IMAGE_NOT_FOUND,
					requestParameters, HttpStatus.NOT_FOUND, request,
					BackgroundImageValueObjectConstants.VIEW_BACKGROUND_IMAGE);
		} else {
			if (backgroundImages.getBackgroundImages().size() == 1) {
				responseObject = backgroundImages.getBackgroundImages().get(0);
				entityName = BACKGROUND_IMAGE_PARENT_ENTITY_NAME;
			} else {
				responseObject = backgroundImages;
				entityName = BACKGROUND_IMAGES_PARENT_ENTITY_NAME;
			}
		}

		return createSuccessResponseEntity(entityName, requestParameters,
				responseObject, request,
				BackgroundImageValueObjectConstants.VIEW_BACKGROUND_IMAGE);
	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/background-image",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/background-image" }, method = RequestMethod.GET)
	public ResponseEntity<String> getackgroundImage(
			@Valid ViewBackgroundImageRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		BackgroundImageValueObject backgroundImage = backgroundImageService
				.getRandomBackgroundImage(requestParameters);

		if (backgroundImage == null) {
			return createErrorResponseEntity(
					ErrorConstants.BACKGROUND_IMAGE_NOT_FOUND,
					ErrorCodeConstants.BACKGROUND_IMAGE_NOT_FOUND,
					requestParameters, HttpStatus.NOT_FOUND, request,
					BackgroundImageValueObjectConstants.VIEW_BACKGROUND_IMAGE);
		}

		return createSuccessResponseEntity(BACKGROUND_IMAGE_PARENT_ENTITY_NAME,
				requestParameters, backgroundImage, request,
				BackgroundImageValueObjectConstants.VIEW_BACKGROUND_IMAGE);
	}
}
