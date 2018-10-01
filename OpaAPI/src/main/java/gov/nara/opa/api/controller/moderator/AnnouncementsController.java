package gov.nara.opa.api.controller.moderator;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import gov.nara.opa.api.services.moderator.AnnouncementsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.moderator.AnnouncementsRequestParameters;
import gov.nara.opa.api.validation.moderator.UpdateAnnouncementsRequestParameters;
import gov.nara.opa.api.valueobject.moderator.AnnouncementValueObject;
import gov.nara.opa.api.valueobject.moderator.AnnouncementValueObjectConstants;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;

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
public class AnnouncementsController extends AbstractBaseController {

	@Autowired
	private AnnouncementsService announcementsService;

	public static final String ANNOUNCEMENT_PARENT_ENTITY_NAME = "announcement";

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/announcements",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/announcements" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewAnnouncement(
			AnnouncementsRequestParameters requestParameters,
			HttpServletRequest request) {

		AnnouncementValueObject announcement = announcementsService
				.getAnnouncement();

		if (announcement == null) {
			return createErrorResponseEntity(
					ErrorConstants.ANNOUNCEMENT_NOT_FOUND,
					ErrorCodeConstants.ANNOUNCEMENT_NOT_FOUND,
					requestParameters, HttpStatus.NOT_FOUND, request,
					AnnouncementValueObjectConstants.VIEW_ANNOUNCEMENT);
		}

		return createSuccessResponseEntity(ANNOUNCEMENT_PARENT_ENTITY_NAME,
				requestParameters, announcement, request,
				AnnouncementValueObjectConstants.VIEW_ANNOUNCEMENT);
	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/announcements",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/announcements" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewAnnouncementModerator(
			AnnouncementsRequestParameters requestParameters,
			HttpServletRequest request) {

		AnnouncementValueObject announcement = announcementsService
				.getAnnouncementForModerator();

		if (announcement == null) {
			return createErrorResponseEntity(
					ErrorConstants.ANNOUNCEMENT_NOT_FOUND,
					ErrorCodeConstants.ANNOUNCEMENT_NOT_FOUND,
					requestParameters,
					HttpStatus.NOT_FOUND,
					request,
					AnnouncementValueObjectConstants.VIEW_ANNOUNCEMENT_MODERATOR);
		}

		return createSuccessResponseEntity(ANNOUNCEMENT_PARENT_ENTITY_NAME,
				requestParameters, announcement, request,
				AnnouncementValueObjectConstants.VIEW_ANNOUNCEMENT_MODERATOR);
	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/announcements",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/announcements" }, method = RequestMethod.PUT)
	public ResponseEntity<String> updateAnnouncement(
			@Valid UpdateAnnouncementsRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		AnnouncementValueObject announcement = announcementsService
				.updateAnnouncement(requestParameters);

		if (announcement == null) {
			return createErrorResponseEntity(
					ErrorConstants.ANNOUNCEMENT_NOT_FOUND,
					ErrorCodeConstants.ANNOUNCEMENT_NOT_FOUND,
					requestParameters, HttpStatus.NOT_FOUND, request,
					AnnouncementValueObjectConstants.UPDATE_ANNOUNCEMENT);
		}

		return createSuccessResponseEntity(ANNOUNCEMENT_PARENT_ENTITY_NAME,
				requestParameters, announcement, request,
				AnnouncementValueObjectConstants.UPDATE_ANNOUNCEMENT);
	}
}
