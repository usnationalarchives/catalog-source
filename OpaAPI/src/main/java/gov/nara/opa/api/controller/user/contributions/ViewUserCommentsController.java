package gov.nara.opa.api.controller.user.contributions;

import com.searchtechnologies.aspire.services.AspireObject;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.user.contributions.UserContributionsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.contributions.UserContributionsErrorCode;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.contributions.UserContributionsValidator;
import gov.nara.opa.api.valueobject.user.contributions.UserContributedCommentValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class ViewUserCommentsController {
	private static OpaLogger logger = OpaLogger
			.getLogger(ViewUserCommentsController.class);

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private UserContributionsValidator userContributionsValidator;

	@Autowired
	private UserContributionsService userContributionsService;

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/contributions/comments",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/contributions/comments" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewUserComments(
			HttpServletRequest request,
			@RequestParam(value = "username", required = false, defaultValue = "") String userName,
			@RequestParam(value = "title", required = false, defaultValue = "") String title,
			@RequestParam(value = "descOrder", required = false, defaultValue = "true") boolean descOrder,
			@RequestParam(value = "rows", required = false, defaultValue = "25") int rows,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

		String message = "";
		HttpStatus status = HttpStatus.OK;
		String action = "viewUserComments";
		String resultsType = "comments";

		// Build the Aspire OPA response object
		AspireObject responseObj = new AspireObject("opaResponse");

		// Get the request path
		String requestPath = PathUtils.getServeletPath(request);

		// Set request params
		LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
		requestParams.put("@path", requestPath);
		requestParams.put("action", action);
		requestParams.put("title", title);
		requestParams.put("descOrder", descOrder);
		requestParams.put("username", userName);
		requestParams.put("rows", rows);
		requestParams.put("offset", offset);
		requestParams.put("format", format);
		requestParams.put("pretty", pretty);

		try {

			// Validate parameters
			String[] paramNamesStringArray = { "username", "rows", "offset",
					"sort", "format", "pretty", "descOrder", "title" };
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
				userContributionsValidator.validateParameters(requestParams,
						false);
			}

			if (userContributionsValidator.getIsValid()) {
				if (!userContributionsService.isValidUserName(userName)) {
					status = HttpStatus.NOT_FOUND;
					userContributionsValidator
							.setStandardError(UserContributionsErrorCode.USER_NOT_FOUND);
				} else {
					// Set header params
					LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
					headerParams.put("@status", String.valueOf(status));
					headerParams.put("time",
							TimestampUtils.getUtcTimestampString());

					// Add all the info found to the response object
					LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
					LinkedHashMap<String, Object> resultsMapComments = new LinkedHashMap<String, Object>();
					ArrayList<LinkedHashMap<String, Object>> commentsResultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

					// Retrieve the comment information
					List<UserContributedCommentValueObject> commentList = new ArrayList<UserContributedCommentValueObject>();

					commentList = userContributionsService.selectUserComments(
							userName, title, offset, rows, descOrder);

					// Set response values
					if (commentList != null && commentList.size() > 0) {

						// Get total of contributed comments of the user logged
						resultsMapComments.put("total",
								userContributionsService
										.getTotalComments(userName));

						for (UserContributedCommentValueObject commentObj : commentList) {
							LinkedHashMap<String, Object> resultValuesMap = commentObj
									.getAspireObjectContent("");

							commentsResultsValueList.add(resultValuesMap);
						}

						resultsMapComments.put("comment",
								commentsResultsValueList);
						resultsMap.put("comments", resultsMapComments);
					}

					if (resultsMap == null || resultsMap.size() == 0) {
						status = HttpStatus.NOT_FOUND;
						userContributionsValidator
								.setStandardError(UserContributionsErrorCode.CONTRIBUTIONS_NOT_FOUND);

					} else {
						// Add request params to response
						headerParams.put("request", requestParams);

						// Set response header
						apiResponse.setResponse(responseObj, "header",
								headerParams);

						// Set response results
						apiResponse.setResponse(responseObj, resultsType,
								resultsMap);
					}
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
				errorValuesMap.put("descOrder", descOrder);
				errorValuesMap.put("username", userName);
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