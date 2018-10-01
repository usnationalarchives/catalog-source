package gov.nara.opa.api.controller.user.contributions;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.user.contributions.UserContributionsService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.user.accounts.UserAccountResponseValuesHelper;
import gov.nara.opa.api.user.contributions.UserContributionsErrorCode;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.contributions.UserContributionsValidator;
import gov.nara.opa.api.valueobject.user.contributions.UserContributionValueObject;
import gov.nara.opa.api.valueobject.user.lists.UserListCollectionValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

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
public class ViewUserSummaryController {

	private static OpaLogger logger = OpaLogger
			.getLogger(ViewUserSummaryController.class);

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private UserContributionsValidator userContributionsValidator;

	@Autowired
	private UserContributionsService userContributionsService;

	@Autowired
	private ViewUserListService viewUserListService;

	@Autowired
	private UserAccountResponseValuesHelper aspireObjectHelper;

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/accounts/summary",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/accounts/summary" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewUserSummary(
			HttpServletRequest request,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

		String message = "";
		HttpStatus status = HttpStatus.OK;
		String action = "viewUserSummary";

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
				userContributionsValidator
						.setStandardError(UserContributionsErrorCode.INVALID_PARAMETER);
				userContributionsValidator
						.setMessage(ErrorConstants.invalidParameterName);
				userContributionsValidator.setIsValid(false);
			} else {
				userContributionsValidator.validateParameters(requestParams);
			}

			if (userContributionsValidator.getIsValid()) {

				// Set header params
				LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
				headerParams.put("@status", String.valueOf(status));
				headerParams
						.put("time", TimestampUtils.getUtcTimestampString());

				// Add all the info found to the response object
				LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();

				// Get the data from the database
				UserContributionValueObject userContributions = userContributionsService
						.getUserContributionsDetailSummary(userAccount
								.getAccountId());

				LinkedHashMap<String, Object> resultsMapTags = new LinkedHashMap<String, Object>();
				resultsMapTags.put("@total", userContributions.getTotalTags());
				resultsMapTags.put("@totalMonth",
						userContributions.getTotalTagsMonth());
				resultsMapTags.put("@totalYear",
						userContributions.getTotalTagsYear());

				LinkedHashMap<String, Object> resultsMapTranscriptions = new LinkedHashMap<String, Object>();
				resultsMapTranscriptions.put("@total",
						userContributions.getTotalTranscriptions());
				resultsMapTranscriptions.put("@totalMonth",
						userContributions.getTotalTranscriptionsMonth());
				resultsMapTranscriptions.put("@totalYear",
						userContributions.getTotalTranscriptionsYear());
				
				LinkedHashMap<String, Object> resultsMapComments = new LinkedHashMap<String, Object>();
				resultsMapComments.put("@total",
						userContributions.getTotalComments());
				resultsMapComments.put("@totalMonth",
						userContributions.getTotalCommentsMonth());
				resultsMapComments.put("@totalYear",
						userContributions.getTotalCommentsYear());

				resultsMap.put("@total",
						userContributions.getTotalContributions());
				resultsMap.put("@totalMonth",
						userContributions.getTotalContributionsMonth());
				resultsMap.put("@totalYear",
						userContributions.getTotalContributionsYear());
				resultsMap.put("tags", resultsMapTags);
				resultsMap.put("transcriptions", resultsMapTranscriptions);
				resultsMap.put("comments", resultsMapComments);

				// Retrieve the collection of lists filtering by accountId
				UserListCollectionValueObject resultObject = viewUserListService
						.viewMyLists(userAccount.getAccountId(), 0, 0);

				Integer listCount = resultObject.getTotalLists();

				// Add all the lists found to the response object
				LinkedHashMap<String, Object> resultsMapLists = new LinkedHashMap<String, Object>();
				resultsMapLists.put("@total", listCount);

				// Add request params to response
				headerParams.put("request", requestParams);

				// Set response header
				apiResponse.setResponse(responseObj, "header", headerParams);

				// Set response results for Contributions
				apiResponse.setResponse(responseObj, "contributions",
						resultsMap);

				// Set response results for Lists
				apiResponse.setResponse(responseObj, "lists", resultsMapLists);

				apiResponse.setResponse(responseObj, "account",
						aspireObjectHelper.getResponseValues(userAccount));

			}

			if (!userContributionsValidator.getIsValid()) {

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
				errorValuesMap.put("@code", userContributionsValidator
						.getErrorCode().toString());
				errorValuesMap.put("description", userContributionsValidator
						.getErrorCode().getErrorMessage());
				errorValuesMap.put("@path", requestPath);
				errorValuesMap.put("action", action);
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
