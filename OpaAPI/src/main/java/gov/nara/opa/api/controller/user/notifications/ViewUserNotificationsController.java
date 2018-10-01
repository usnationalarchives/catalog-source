package gov.nara.opa.api.controller.user.notifications;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.user.notifications.UserNotificationsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.moderator.NotificationsValidator;
import gov.nara.opa.api.valueobject.user.notifications.UserNotificationErrorCode;
import gov.nara.opa.api.valueobject.user.notifications.UserNotificationValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewUserNotificationsController {

	private static OpaLogger logger = OpaLogger
			.getLogger(ViewUserNotificationsController.class);

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private NotificationsValidator notificationsValidator;

	@Autowired
	private UserNotificationsService userNotificationsService;

	@RequestMapping(value = { "/" + Constants.INTERNAL_API_PATH + "/"
			+ Constants.API_VERS_NUM + "/accounts/notifications" }, method = RequestMethod.GET)
	public ResponseEntity<String> getNotifications(
			HttpServletRequest request,
			@RequestParam(value = "rows", required = false, defaultValue = "200") int rows,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

		String message = "";
		HttpStatus status = HttpStatus.OK;
		String action = "viewUserNotification";
		String resultType = "notification";
		String resultsType = "notifications";

		// Build the Aspire OPA response object
		AspireObject responseObj = new AspireObject("opaResponse");

		// Get the request path
		String requestPath = PathUtils.getServeletPath(request);

		// Set request params
		LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
		requestParams.put("@path", requestPath);
		requestParams.put("action", action);
		requestParams.put("rows", rows);
		requestParams.put("offset", offset);
		requestParams.put("format", format);
		requestParams.put("pretty", pretty);

		try {

			// Retrieve the user account object
			Authentication auth = SecurityContextHolder.getContext()
					.getAuthentication();
			UserAccount userAccount = (UserAccount) auth.getDetails();

			// Validate parameters
			String[] paramNamesStringArray = { "rows", "offset", "format",
					"pretty" };
			LinkedHashMap<String, String> validRequestParameterNames = StringUtils
					.convertStringArrayToLinkedHashMap(paramNamesStringArray);
			if (!ValidationUtils.validateRequestParameterNames(
					validRequestParameterNames, request.getQueryString())) {
				status = HttpStatus.BAD_REQUEST;
				notificationsValidator
						.setStandardError(UserNotificationErrorCode.INVALID_PARAMETER);
				notificationsValidator
						.setMessage(ErrorConstants.invalidParameterName);
				notificationsValidator.setIsValid(false);
			} else {
				notificationsValidator.validateParameters(requestParams);
			}

			if (notificationsValidator.getIsValid()) {

				// Retrieve the collection of all the Notifications
				List<UserNotificationValueObject> notifications = userNotificationsService
						.viewNotifications(userAccount.getAccountId(), offset,
								rows);

				if (notifications == null || notifications.size() == 0) {
					status = HttpStatus.NOT_FOUND;
					notificationsValidator
							.setStandardError(UserNotificationErrorCode.NOTIFICATIONS_NOT_FOUND);
				} else {
					int totalNewNotifications = 0;
					// Set header params
					LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
					headerParams.put("@status", String.valueOf(status));
					headerParams.put("time",
							TimestampUtils.getUtcTimestampString());

					// Add all the lists found to the response object
					LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
					ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

					for (UserNotificationValueObject notification : notifications) {
						LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
						LinkedHashMap<String, Object> eventMap = new LinkedHashMap<String, Object>();
						LinkedHashMap<String, Object> opaMap = new LinkedHashMap<String, Object>();

						resultValuesMap.put("@on",
								String.valueOf(notification.getOpaType()));

						eventMap.put("@type", notification.getAction());
						eventMap.put("@on", getTypeFullname(notification
								.getAnnotationType()));
						eventMap.put("@when", TimestampUtils
								.getUtcString(notification.getLogTs()));
						eventMap.put("@otherUser",
								String.valueOf(notification.getUserName()));
						eventMap.put("@otherFullName",
								notification.getFullName());
						eventMap.put("@isNaraStaff", notification.isNaraStaff());
						eventMap.put("@displayNameFlag",
								notification.getDisplayNameFlag());
						resultValuesMap.put("event", eventMap);

						resultValuesMap
								.put("title", notification.getOpaTitle());

						opaMap.put("@naId", notification.getNaId());
						opaMap.put("@objectId",
								String.valueOf(notification.getObjectId()));
						opaMap.put("@pageNum",
								String.valueOf(notification.getPageNum()));
						opaMap.put("@totalPages",
								String.valueOf(notification.getTotalPages()));
						resultValuesMap.put("item", opaMap);
						resultsValueList.add(resultValuesMap);

						// increase count of notifications added since last
						// "clear"
						if (notification.getLogId() > notification
								.getLastNotificationId())
							totalNewNotifications++;

					}

					resultsMap.put("total", notifications.size());

					resultsMap.put("totalNew", totalNewNotifications);
					resultsMap.put(resultType, resultsValueList);

					requestParams.put("offset", offset);
					requestParams.put("rows", rows);
					// Add request params to response
					headerParams.put("request", requestParams);

					// Set response header
					apiResponse
							.setResponse(responseObj, "header", headerParams);

					// Set response results
					apiResponse.setResponse(responseObj, resultsType,
							resultsMap);
				}

			}

			if (!notificationsValidator.getIsValid()) {

				// Set error status if different from 404
				if (status != HttpStatus.NOT_FOUND)
					status = HttpStatus.BAD_REQUEST;

				// Set header params
				LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
				headerParams.put("@status", String.valueOf(status));
				headerParams
						.put("time", TimestampUtils.getUtcTimestampString());

				// Set request params
				LinkedHashMap<String, Object> badRequestParams = new LinkedHashMap<String, Object>();
				badRequestParams.put("@path", requestPath);
				badRequestParams.put("action", action);
				headerParams.put("request", badRequestParams);

				// Set error params
				LinkedHashMap<String, Object> errorValuesMap = new LinkedHashMap<String, Object>();
				errorValuesMap.put("@code", notificationsValidator
						.getErrorCode().toString());
				errorValuesMap.put("description", notificationsValidator
						.getErrorCode().getErrorMessage());
				errorValuesMap.put("offset", offset);
				errorValuesMap.put("rows", rows);
				errorValuesMap.put("format", format);
				errorValuesMap.put("pretty", pretty);

				// Set response header
				apiResponse.setResponse(responseObj, "header", headerParams);

				// Set response error
				apiResponse.setResponse(responseObj, "error", errorValuesMap);
			}

			// Build response string using the response object
			message = apiResponse.getResponseOutputString(responseObj, format,
					pretty);

			// Close Aspire response object
			responseObj.close();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException( e );
		}

		ResponseEntity<String> entity = new ResponseEntity<String>(message,
				status);
		return entity;
	}

	/**
	 * Set the type full name of the annotation based on the saved abreviation
	 * 
	 * @param fullname
	 *            fullname of the type, on the format that it received on the
	 *            response
	 * 
	 * @return The abbreviation of the type on the format that is going to be
	 *         stored on the database
	 */
	public String getTypeFullname(String abbreviation) {
		switch (abbreviation) {
		case "TR":
			return "transcriptions";
		case "TG":
			return "tags";
		case "CM":
			return "comments";
		default:
			return "abbreviation";
		}
	}
}