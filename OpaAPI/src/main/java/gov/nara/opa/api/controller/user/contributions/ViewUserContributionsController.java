package gov.nara.opa.api.controller.user.contributions;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.user.contributions.UserContributionsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.contributions.UserContributionsErrorCode;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.contributions.UserContributionsValidator;
import gov.nara.opa.api.valueobject.user.contributions.UserContributionValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewUserContributionsController {

	private static OpaLogger logger = OpaLogger
			.getLogger(ViewUserContributionsController.class);

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private UserContributionsValidator userContributionsValidator;

	@Autowired
	private UserContributionsService userContributionsService;

	@Autowired
	private UserAccountDao userAccountDao;

	@RequestMapping(value = { "/" + Constants.INTERNAL_API_PATH + "/"
			+ Constants.API_VERS_NUM + "/contributions/summary" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewUserContributions(
			HttpServletRequest request,
			@RequestParam(value = "fullstats", required = false, defaultValue = "false") boolean fullStats,
			@RequestParam(value = "username", required = false, defaultValue = "") String userName,
			@RequestParam(value = "rows", required = false, defaultValue = "25") int rows,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
		int accountId = 0;
		String message = "";
		String userFullName = "";
		boolean isDisplayFullNameFlag = true;
		boolean isNaraStaff = true;
		HttpStatus status = HttpStatus.OK;
		String action = "viewUserContributions";
		String resultsType = "contributions";

		// Build the Aspire OPA response object
		AspireObject responseObj = new AspireObject("opaResponse");

		// Get the request path
		String requestPath = PathUtils.getServeletPath(request);

		// Set request params
		LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
		requestParams.put("@path", requestPath);
		requestParams.put("action", action);
		requestParams.put("fullstats", fullStats);
		requestParams.put("rows", rows);
		requestParams.put("offset", offset);
		requestParams.put("format", format);
		requestParams.put("pretty", pretty);

		try {

			// Validate parameters
			String[] paramNamesStringArray = { "username", "rows", "offset",
					"fullstats", "format", "pretty" };
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

				if (userAccountDao.verifyIfUserNameExists(userName)) {
					// Get the account user_name
					UserAccountValueObject user = userAccountDao
							.selectByUserName(userName);
					accountId = user.getAccountId();
					userFullName = user.getFullName();
					isDisplayFullNameFlag = user.getDisplayFullName();
					isNaraStaff = user.isNaraStaff();
				} else {
					status = HttpStatus.NOT_FOUND;
					userContributionsValidator
							.setStandardError(UserContributionsErrorCode.USER_NOT_FOUND);
				}

				if (userContributionsValidator.getIsValid()) {

					// Set header params
					LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
					headerParams.put("@status", String.valueOf(status));
					headerParams.put("time",
							TimestampUtils.getUtcTimestampString());

					// Add all the info found to the response object
					LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();

					// If the Brief Summary was requested:
					if (fullStats == false) {

						// Get the data from the database
						UserContributionValueObject userContributions = userContributionsService
								.getUserContributionsBriefSummary(accountId);

						LinkedHashMap<String, Object> resultsMapTags = new LinkedHashMap<String, Object>();
						resultsMap.put("@userFullName", userFullName);
						resultsMap.put("@DisplayFullNameFlag",
								isDisplayFullNameFlag);
						resultsMap.put("@isNaraStaff", isNaraStaff);

						resultsMapTags.put("@total",
								userContributions.getTotalTags());
						LinkedHashMap<String, Object> resultsMapTranscriptions = new LinkedHashMap<String, Object>();
						resultsMapTranscriptions.put("@total",
								userContributions.getTotalTranscriptions());
						LinkedHashMap<String, Object> resultsMapComments = new LinkedHashMap<String, Object>();
						resultsMapComments.put("@total",
								userContributions.getTotalComments());

						resultsMap.put("@total",
								userContributions.getTotalContributions());
						resultsMap.put("tags", resultsMapTags);
						resultsMap.put("transcriptions",
								resultsMapTranscriptions);
						resultsMap.put("comments",
								resultsMapComments);
						// If the Detailled Summary was requested:
					} else {

						// Get the data from the database
						UserContributionValueObject userContributions = userContributionsService
								.getUserContributionsDetailSummary(accountId);

						LinkedHashMap<String, Object> resultsMapTags = new LinkedHashMap<String, Object>();
						resultsMap.put("@userFullName", userFullName);
						resultsMap.put("@DisplayFullNameFlag",
								isDisplayFullNameFlag);
						resultsMap.put("@isNaraStaff", isNaraStaff);

						resultsMapTags.put("@total",
								userContributions.getTotalTags());
						resultsMapTags.put("@totalMonth",
								userContributions.getTotalTagsMonth());
						resultsMapTags.put("@totalYear",
								userContributions.getTotalTagsYear());

						LinkedHashMap<String, Object> resultsMapTranscriptions = new LinkedHashMap<String, Object>();
						resultsMapTranscriptions.put("@total",
								userContributions.getTotalTranscriptions());
						resultsMapTranscriptions
								.put("@totalMonth", userContributions
										.getTotalTranscriptionsMonth());
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
						resultsMap.put("transcriptions",
								resultsMapTranscriptions);
						resultsMap.put("comments", resultsMapComments);
					}

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
				errorValuesMap.put("fullStats", fullStats);
				errorValuesMap.put("rows", rows);
				errorValuesMap.put("offset", offset);
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
