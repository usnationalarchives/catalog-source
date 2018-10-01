package gov.nara.opa.api.controller.annotation.summary;

import gov.nara.opa.api.annotation.summary.SummaryErrorCode;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.user.contributions.UserContributionsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.summary.SummaryRequestParameters;
import gov.nara.opa.api.validation.annotation.summary.SummaryValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.searchtechnologies.aspire.services.AspireObject;

@Controller
public class SummaryController extends AbstractBaseController {

	private static OpaLogger logger = OpaLogger
			.getLogger(SummaryController.class);

	public static final String VIEW_ACTION = "viewContributionsSummary";

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private SummaryValidator validator;

	@Autowired
	private UserContributionsService userContributionsService;

	@Autowired
	private UserAccountDao userAccountDao;

	/**
	 * Obtain a summary of the annotations
	 * 
	 * @param request
	 *            The HttpServletRequest instance
	 * @param naId
	 *            Parameter that will be used to filter the specified data
	 * @param include
	 *            Specify what entities will be displayed on the summary
	 * @param format
	 *            The output format. Either json or xml. Default is xml
	 * @param pretty
	 *            Specifies if output should be pretty printed. Default is true
	 * @return ResponseEntity with the json/xml representation of either the
	 *         summary or any encountered error.
	 */
	@RequestMapping(value = { "/" + Constants.INTERNAL_API_PATH + "/"
			+ Constants.API_VERS_NUM + "/contributions/summary/id/{naId}", }, method = RequestMethod.GET)
	public ResponseEntity<String> viewAnnotationsSummaryWithNoObject(
			HttpServletRequest request,
			@PathVariable String naId,
			@RequestParam(value = "username", required = false, defaultValue = "") String userName,
			@RequestParam(value = "rows", required = false, defaultValue = "25") int rows,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "include", required = false) String include,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
		ResponseEntity<String> entity = viewAnnotationsSummary(request,
				userName, naId, include, "", format, pretty, offset, rows);

		return entity;
	}

	/**
	 * Obtain a summary of the annotations
	 * 
	 * @param request
	 *            The HttpServletRequest instance
	 * @param naId
	 *            Parameter that will be used to filter the specified data
	 * @param include
	 *            Specify what entities will be displayed on the summary
	 * @param objectId
	 *            Parameter that will be used to filter the specified data
	 * @param format
	 *            The output format. Either json or xml. Default is xml
	 * @param pretty
	 *            Specifies if output should be pretty printed. Default is true
	 * @return ResponseEntity with the json/xml representation of either the
	 *         summary or any encountered error.
	 */
	@RequestMapping(value = { "/" + Constants.INTERNAL_API_PATH + "/"
			+ Constants.API_VERS_NUM
			+ "/contributions/summary/id/{naId}/objects/{objectId:.+}", }, method = RequestMethod.GET)
	public ResponseEntity<String> viewAnnotationsSummaryWithObject(
			HttpServletRequest request,
			@PathVariable String naId,
			@PathVariable String objectId,
			@RequestParam(value = "username", required = false, defaultValue = "") String userName,
			@RequestParam(value = "rows", required = false, defaultValue = "25") int rows,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "include", required = false) String include,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
		ResponseEntity<String> entity = viewAnnotationsSummary(request,
				userName, naId, include, objectId, format, pretty, offset, rows);

		return entity;
	}

	/**
	 * Obtain a summary of the annotations
	 * 
	 * @param request
	 *            The HttpServletRequest instance
	 * @param naId
	 *            Parameter that will be used to filter the specified data
	 * @param include
	 *            Specify what entities will be displayed on the summary
	 * @param objectId
	 *            Parameter that will be used to filter the specified data
	 * @param format
	 *            The output format. Either json or xml. Default is xml
	 * @param pretty
	 *            Specifies if output should be pretty printed. Default is true
	 * @return ResponseEntity with the json/xml representation of either the
	 *         summary or any encountered error.
	 */
	public ResponseEntity<String> viewAnnotationsSummary(
			HttpServletRequest request, String userName, String naId,
			String include, String objectId, String format, boolean pretty,
			int offset, int rows) {

		int accountId = 0;
		String message = "";
		HttpStatus status = HttpStatus.OK;
		String resultsType = "contributions";

		// Build the Aspire OPA response object
		AspireObject responseObj = new AspireObject("opaResponse");

		// Get the request path
		String requestPath = PathUtils.getServeletPath(request);

		// Set request params
		LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
		requestParams.put("@path", requestPath);
		requestParams.put("action", VIEW_ACTION);
		requestParams.put("naId", naId);
		if (objectId != null && !objectId.equals(""))
			requestParams.put("objectId", objectId);
		requestParams.put("offset", offset);
		requestParams.put("rows", rows);
		requestParams.put("include", include);
		requestParams.put("format", format);
		requestParams.put("pretty", pretty);

		try {

			// Validate parameters
			String[] paramNamesStringArray = { "username", "rows", "offset",
					"include", "format", "pretty" };
			LinkedHashMap<String, String> validRequestParameterNames = StringUtils
					.convertStringArrayToLinkedHashMap(paramNamesStringArray);
			if (!ValidationUtils.validateRequestParameterNames(
					validRequestParameterNames, request.getQueryString())) {
				status = HttpStatus.BAD_REQUEST;
				validator.setStandardError(SummaryErrorCode.INVALID_PARAMETER);
				validator.setMessage(ErrorConstants.invalidParameterName);
				validator.setIsValid(false);
			} else {
				validator.validateParameters(requestParams);
			}
			if (validator.getIsValid()) {
				if (userAccountDao.verifyIfUserNameExists(userName)) {
					// Get the account user_name
					UserAccountValueObject user = userAccountDao
							.selectByUserName(userName);
					accountId = user.getAccountId();
				} else {
					status = HttpStatus.NOT_FOUND;
					validator.setStandardError(SummaryErrorCode.USER_NOT_FOUND);
				}
				if (validator.getIsValid()) {
					// Set header params
					LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
					headerParams.put("@status", String.valueOf(status));
					headerParams.put("time",
							TimestampUtils.getUtcTimestampString());

					// Add all the lists found to the response object
					LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();

					String[] annotationTypes = include.split(",");

					for (String type : annotationTypes) {

						switch (type) {
						case "tags":
							LinkedHashMap<String, Object> resultsMapTags = new LinkedHashMap<String, Object>();
							ArrayList<LinkedHashMap<String, Object>> tagsResultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

							// Retrieve the tag information
							List<TagValueObject> tagList = new ArrayList<TagValueObject>();
							tagList = userContributionsService.viewTags(
									accountId, naId, objectId, "", 1, offset,
									rows);

							// Set response values
							if (tagList != null && tagList.size() > 0) {

								resultsMapTags.put("total", tagList.size());
								for (TagValueObject tagObj : tagList) {
									LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
									resultValuesMap.put("@text",
											tagObj.getAnnotation());
									resultValuesMap.put("@user",
											tagObj.getUserName());
									resultValuesMap.put("@fullName",
											tagObj.getFullName());
									resultValuesMap.put("@isNaraStaff", String
											.valueOf(tagObj.getIsNaraStaff()));
									resultValuesMap.put("@created",
											TimestampUtils.getUtcString(tagObj
													.getAnnotationTS()));
									tagsResultsValueList.add(resultValuesMap);
								}
								resultsMapTags.put("tag", tagsResultsValueList);
								resultsMap.put("tags", resultsMapTags);
							}
							break;
						case "transcriptions":
							LinkedHashMap<String, Object> resultsMapTranscriptions = new LinkedHashMap<String, Object>();
							ArrayList<LinkedHashMap<String, Object>> transcriptionResultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

							// Retrieve the Transcriptions collection
							List<Transcription> transcriptionList = new ArrayList<Transcription>();

							transcriptionList = userContributionsService
									.viewTranscriptions(accountId, naId,
											objectId, offset, rows);

							// Set response values
							if (transcriptionList != null
									&& transcriptionList.size() > 0) {

								resultsMapTranscriptions.put("total",
										transcriptionList.size());
								for (Transcription transcriptionObj : transcriptionList) {
									LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
									resultValuesMap.put("@text",
											transcriptionObj.getAnnotation());
									resultValuesMap.put("@user",
											transcriptionObj.getUserName());
									resultValuesMap.put("@fullName",
											transcriptionObj.getFullName());
									resultValuesMap.put("@isNaraStaff", String
											.valueOf(transcriptionObj
													.getIsNaraStaff()));
									resultValuesMap
											.put("@created",
													TimestampUtils
															.getUtcString(transcriptionObj
																	.getAnnotationTS()));
									transcriptionResultsValueList
											.add(resultValuesMap);
								}
								resultsMapTranscriptions.put("transcription",
										transcriptionResultsValueList);
								resultsMap.put("transcriptions",
										resultsMapTranscriptions);
							}
							break;
						default:
							break;
						}
					}

					if (resultsMap == null || resultsMap.size() == 0) {
						status = HttpStatus.NOT_FOUND;
						validator
								.setStandardError(SummaryErrorCode.CONTRIBUTIONS_NOT_FOUND);

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

			if (!validator.getIsValid()) {

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
				badRequestParams.put("action", VIEW_ACTION);
				headerParams.put("request", badRequestParams);

				// Set error params
				LinkedHashMap<String, Object> errorValuesMap = new LinkedHashMap<String, Object>();
				errorValuesMap
						.put("@code", validator.getErrorCode().toString());
				errorValuesMap.put("description", validator.getErrorCode()
						.getErrorMessage());
				errorValuesMap.put("naId", naId);
				errorValuesMap.put("include", include);
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
		}

		ResponseEntity<String> entity = new ResponseEntity<String>(message,
				status);
		return entity;
	}

	public ResponseEntity<String> viewAnnotationsSummaryWithObject_New(
			@Valid SummaryRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = validator.validate(bindingResult,
				request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					VIEW_ACTION);
		}

		return null;
	}

}
