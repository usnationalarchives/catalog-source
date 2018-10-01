package gov.nara.opa.api.controller.moderator;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import gov.nara.opa.api.services.moderator.OnlineAvailabilityHeaderService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.moderator.OnlineAvailabilityHeaderModeratorRequestParameters;
import gov.nara.opa.api.validation.moderator.OnlineAvailabilityHeaderRequestParameters;
import gov.nara.opa.api.validation.moderator.OnlineAvailabilityHeaderValidator;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObject;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObjectConstants;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;

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
public class OnlineAvailabilityHeaderController extends AbstractBaseController {

	@Autowired
	private OnlineAvailabilityHeaderService onlineAvailabilityHeaderService;

	@Autowired
	private OnlineAvailabilityHeaderValidator onlineAvailabilityHeaderValidator;

	public static final String ONLINE_AVAILABILITY_HEADER_PARENT_ENTITY_NAME = "online-availability-header";

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/online-availability/{naId}",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/online-availability/{naId}" }, method = RequestMethod.GET)
	public ResponseEntity<String> getOnlineAvailabilityByNaId(
			@Valid OnlineAvailabilityHeaderRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		ValidationResult validationResult = onlineAvailabilityHeaderValidator
				.validate(bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(
					validationResult,
					request,
					OnlineAvailabilityHeaderValueObjectConstants.VIEW_ONLINE_AVAILABILITY_HEADER);
		}

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = onlineAvailabilityHeaderService
				.getOnlineAvailabilityHeaderByNaIdForModerator(requestParameters.getNaId());
		if (onlineAvailabilityHeader == null) {
			return createErrorResponseEntity(ErrorConstants.INVALID_NA_ID, 
					ArchitectureErrorCodeConstants.INVALID_ID_VALUE,
					requestParameters,
					HttpStatus.NOT_FOUND,
					request,
					OnlineAvailabilityHeaderValueObjectConstants.VIEW_ONLINE_AVAILABILITY_HEADER);
		}
		return createSuccessResponseEntity(
				ONLINE_AVAILABILITY_HEADER_PARENT_ENTITY_NAME,
				requestParameters,
				onlineAvailabilityHeader,
				request,
				OnlineAvailabilityHeaderValueObjectConstants.VIEW_ONLINE_AVAILABILITY_HEADER);
	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/online-availability/{naId}",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/online-availability/{naId}" }, method = RequestMethod.POST)
	public ResponseEntity<String> updateOnlineAvailabilityHeader(
			@Valid OnlineAvailabilityHeaderRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		ValidationResult validationResult = onlineAvailabilityHeaderValidator
				.validate(bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(
					validationResult,
					request,
					OnlineAvailabilityHeaderValueObjectConstants.UPDATE_ONLINE_AVAILABILITY_HEADER);
		}

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = onlineAvailabilityHeaderService
				.getOnlineAvailabilityHeaderByNaId(requestParameters.getNaId());

		if (onlineAvailabilityHeader.getHeader() == null
				&& onlineAvailabilityHeader.getAvailabilityTS() == null) {
			onlineAvailabilityHeader = onlineAvailabilityHeaderService
					.addOnlineAvalilabilityHeader(requestParameters);
			if (onlineAvailabilityHeader == null) {
				return createErrorResponseEntity(
						ErrorConstants.ONLINE_AVALIABILITY_HEADER_NOT_FOUND,
						ErrorCodeConstants.ONLINE_AVALIABILITY_HEADER_NOT_FOUND,
						requestParameters,
						HttpStatus.NOT_FOUND,
						request,
						OnlineAvailabilityHeaderValueObjectConstants.UPDATE_ONLINE_AVAILABILITY_HEADER);
			}
		} else {
			onlineAvailabilityHeader = onlineAvailabilityHeaderService
					.updateOnlineAvalilabilityHeader(onlineAvailabilityHeader,
							requestParameters);
		}

		return createSuccessResponseEntity(
				ONLINE_AVAILABILITY_HEADER_PARENT_ENTITY_NAME,
				requestParameters,
				onlineAvailabilityHeader,
				request,
				OnlineAvailabilityHeaderValueObjectConstants.UPDATE_ONLINE_AVAILABILITY_HEADER);
	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/online-availability/{naId}",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/online-availability/{naId}" }, method = RequestMethod.PUT)
	public ResponseEntity<String> restoreOnlineAvailabilityHeader(
			@Valid OnlineAvailabilityHeaderModeratorRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = onlineAvailabilityHeaderService
				.restoreOnlineAvailabilityHeader(requestParameters);

		return createSuccessResponseEntity(
				ONLINE_AVAILABILITY_HEADER_PARENT_ENTITY_NAME,
				requestParameters,
				onlineAvailabilityHeader,
				request,
				OnlineAvailabilityHeaderValueObjectConstants.RESTORE_ONLINE_AVAILABILITY_HEADER);
	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/online-availability/{naId}",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/online-availability/{naId}" }, method = RequestMethod.DELETE)
	public ResponseEntity<String> removeOnlineAvailabilityHeader(
			@Valid OnlineAvailabilityHeaderModeratorRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = onlineAvailabilityHeaderService
				.removeOnlineAvailabilityHeader(requestParameters);

		return createSuccessResponseEntity(
				ONLINE_AVAILABILITY_HEADER_PARENT_ENTITY_NAME,
				requestParameters,
				onlineAvailabilityHeader,
				request,
				OnlineAvailabilityHeaderValueObjectConstants.REMOVE_ONLINE_AVAILABILITY_HEADER);
	}
}
