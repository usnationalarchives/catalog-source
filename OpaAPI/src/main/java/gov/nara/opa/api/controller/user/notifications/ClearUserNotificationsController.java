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
public class ClearUserNotificationsController {
	private static OpaLogger logger = OpaLogger
			.getLogger(ClearUserNotificationsController.class);

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private NotificationsValidator notificationsValidator;

	@Autowired
	private UserNotificationsService userNotificationsService;

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/accounts/notifications",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/accounts/notifications" }, method = RequestMethod.DELETE)
	public ResponseEntity<String> clearNotifications(
			HttpServletRequest request,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

		String message = "";
		HttpStatus status = HttpStatus.OK;
		String action = "clearUserNotification";

		// Build the Aspire OPA response object
		AspireObject responseObj = new AspireObject("opaResponse");

		// Get the request path
		String requestPath = PathUtils.getServeletPath(request);

		// Set request params
		LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
		requestParams.put("@path", requestPath);
		requestParams.put("action", action);
		requestParams.put("format", format);
		requestParams.put("pretty", pretty);

		try {

			// Retrieve the user account object
			Authentication auth = SecurityContextHolder.getContext()
					.getAuthentication();
			UserAccount userAccount = (UserAccount) auth.getDetails();

			// Validate parameters
			String[] paramNamesStringArray = { "format", "pretty" };
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

				// Set header params
				LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
				headerParams.put("@status", String.valueOf(status));
				headerParams
						.put("time", TimestampUtils.getUtcTimestampString());

				// Add request params to response
				headerParams.put("request", requestParams);

				// Retrieve the collection of all the Notifications
				List<UserNotificationValueObject> notifications = userNotificationsService
						.viewNotifications(userAccount.getAccountId(), 0, 1000);

				if (notifications == null || notifications.size() == 0) {
					status = HttpStatus.NOT_FOUND;
					notificationsValidator
							.setStandardError(UserNotificationErrorCode.NOTIFICATIONS_NOT_FOUND);
				} else {

					// Return true if the last_notification_id value was updated
					userNotificationsService.clearNotifications(userAccount
							.getAccountId());
					// Add all the notifications found to the response object

					LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();

					resultsMap.put("@fullName", userAccount.getFullName());
					resultsMap.put("@userName", userAccount.getUserName());
					resultsMap.put("@when",
							TimestampUtils.getUtcTimestampString());
					resultsMap.put("totalNotificationsCleared",
							notifications.size());

					// Set response header
					apiResponse
							.setResponse(responseObj, "header", headerParams);

					// Set response results
					apiResponse.setResponse(responseObj,
							"NotificationsCleared", resultsMap);
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
			// throw new OpaRuntimeException(e);
		}

		ResponseEntity<String> entity = new ResponseEntity<String>(message,
				status);
		return entity;
	}

}
