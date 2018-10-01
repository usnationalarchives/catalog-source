package gov.nara.opa.api.controller.moderator;

import gov.nara.opa.api.moderator.ModeratorStreamResponseValuesHelper;
import gov.nara.opa.api.moderator.contributionsStream.ContributionsStreamErrorCode;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.response.ResponseHelper;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.moderator.ViewModeratorStreamService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.system.logging.APILogger;
import gov.nara.opa.api.utils.AspireObjectUtils;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.contributionsStream.ContributionsStreamValidator;
import gov.nara.opa.architecture.utils.StringUtils;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Retrieves the transaction data as a stream for the moderator to oversee
 */
@Controller
public class ViewModeratorStreamController {

	private static APILogger log = APILogger
			.getLogger(ViewModeratorStreamController.class);

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private ContributionsStreamValidator validator;

	@Autowired
	private ViewModeratorStreamService viewModeratorStreamService;

	@Autowired
	private ModeratorStreamResponseValuesHelper responseValuesHelper;

	/**
	 * Retrieves the moderator stream
	 * 
	 * @param request
	 *            The servlet request instance
	 * @param offset
	 *            The starting offset of the stream
	 * @param rows
	 *            The maxmimum number of rows
	 * @param filterType
	 *            The filtering type, can be 'Moderator' or any of 'TG', 'TR' or
	 *            (TBD)
	 * @param naId
	 * @param format
	 * @param pretty
	 * @return
	 */
	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/stream",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/stream" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewModeratorStream(
			HttpServletRequest request,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "rows", required = false, defaultValue = "25") int rows,
			@RequestParam(value = "filterType", required = false) String filterType,
			@RequestParam(value = "naId", required = false) String naId,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
		String message = "";
		HttpStatus status = HttpStatus.OK;
		AspireObject aspireObject = AspireObjectUtils
				.getAspireObject("opaResponse");
		ContributionsStreamErrorCode contributionsStreamErrorCode = ContributionsStreamErrorCode.NONE;
		String requestPath = PathUtils.getServeletPath(request);
		String responseType = "results";
		String action = "viewModeratorStream";

		LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
		requestParams.put("offset", offset);
		requestParams.put("rows", rows);
		requestParams.put("filterType", filterType);
		requestParams.put("naId", naId);
		requestParams.put("format", format);
		requestParams.put("pretty", pretty);

		// Validate general params
		validator.resetValidation();

		String[] paramNamesStringArray = { "rows", "offset", "filterType",
				"naId", "format", "pretty" };
		LinkedHashMap<String, String> validRequestParameterNames = StringUtils
				.convertStringArrayToLinkedHashMap(paramNamesStringArray);
		if (!ValidationUtils.validateRequestParameterNames(
				validRequestParameterNames, request.getQueryString())) {
			status = HttpStatus.BAD_REQUEST;
			validator
					.setStandardError(ContributionsStreamErrorCode.INVALID_PARAM);
			validator.setMessage(ErrorConstants.invalidParameterName);
			validator.setIsValid(false);
		} else {
			validator.validate(requestParams, false);
		}

		if (validator.getIsValid()) {
			// Service call
			ServiceResponseObject responseObject = viewModeratorStreamService
					.viewModeratorStream(offset, rows, filterType, naId);

			contributionsStreamErrorCode = (ContributionsStreamErrorCode) responseObject
					.getErrorCode();
			if (contributionsStreamErrorCode == ContributionsStreamErrorCode.NONE) {

				// Extract values and call response builder
				responseValuesHelper.Init(responseObject, offset, rows);

				// Call API response setters
				apiResponse.setResponse(aspireObject, "header", ResponseHelper
						.getHeaderItems(status, requestPath, action,
								requestParams));
				apiResponse.setResponse(aspireObject, responseType,
						responseValuesHelper.getResponseValues(filterType
								.equals("Moderator")));

				log.info("viewModeratorStream", String.format("%1$s %2$s",
						"view[stream]", requestParams));

			} else {
				status = HttpStatus.BAD_REQUEST;
			}

		} else {
			contributionsStreamErrorCode = validator.getErrorCode();
			status = HttpStatus.BAD_REQUEST;
		}

		// Determine if an error code was generated
		if (contributionsStreamErrorCode != ContributionsStreamErrorCode.NONE) {

			String errorCodeStr = contributionsStreamErrorCode.toString();
			String errorMessage = contributionsStreamErrorCode
					.getErrorMessage();

			// Call API response setters for error
			apiResponse.setResponse(aspireObject, "header",
					ResponseHelper.getHeaderItems(status, requestPath, action));
			apiResponse.setResponse(aspireObject, "error", ResponseHelper
					.getErrorItems(status, errorCodeStr, errorMessage,
							requestParams));
		}

		message = apiResponse.getResponseOutputString(aspireObject, format,
				pretty);

		AspireObjectUtils.closeAspireObject(aspireObject);

		ResponseEntity<String> entity = new ResponseEntity<String>(message,
				status);
		return entity;

	}

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/contributionTotals",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/contributionTotals" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewContributionTotals(
			HttpServletRequest request,
			@RequestParam(value = "naId", required = false, defaultValue = "") String naId,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
		String message = "";
		HttpStatus status = HttpStatus.OK;
		AspireObject aspireObject = AspireObjectUtils
				.getAspireObject("opaResponse");
		ContributionsStreamErrorCode contributionsStreamErrorCode = ContributionsStreamErrorCode.NONE;
		String requestPath = PathUtils.getServeletPath(request);
		String responseType = "results";
		String action = "viewModeratorStream";

		LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
		requestParams.put("naId", naId);
		requestParams.put("format", format);
		requestParams.put("pretty", pretty);

		// Validate general params
		validator.resetValidation();

		String[] paramNamesStringArray = { "naId", "format", "pretty" };
		LinkedHashMap<String, String> validRequestParameterNames = StringUtils
				.convertStringArrayToLinkedHashMap(paramNamesStringArray);
		if (!ValidationUtils.validateRequestParameterNames(
				validRequestParameterNames, request.getQueryString())) {
			status = HttpStatus.BAD_REQUEST;
			validator
					.setStandardError(ContributionsStreamErrorCode.INVALID_PARAM);
			validator.setMessage(ErrorConstants.invalidParameterName);
			validator.setIsValid(false);
		}

		if (validator.getIsValid()) {
			// Service call
			ServiceResponseObject responseObject = viewModeratorStreamService
					.viewContributionTotals(naId);

			contributionsStreamErrorCode = (ContributionsStreamErrorCode) responseObject
					.getErrorCode();
			if (contributionsStreamErrorCode == ContributionsStreamErrorCode.NONE) {

				// Call API response setters
				apiResponse.setResponse(aspireObject, "header", ResponseHelper
						.getHeaderItems(status, requestPath, action,
								requestParams));
				apiResponse.setResponse(aspireObject, responseType,
						getContributionTotalResponse(responseObject));

				log.info("viewModeratorStream", String.format("%1$s %2$s",
						"view[stream]", requestParams));

			} else {
				status = HttpStatus.BAD_REQUEST;
			}
		} else {
			contributionsStreamErrorCode = validator.getErrorCode();
			status = HttpStatus.BAD_REQUEST;
		}

		// Determine if an error code was generated
		if (contributionsStreamErrorCode != ContributionsStreamErrorCode.NONE) {

			String errorCodeStr = contributionsStreamErrorCode.toString();
			String errorMessage = contributionsStreamErrorCode
					.getErrorMessage();

			// Call API response setters for error
			apiResponse.setResponse(aspireObject, "header",
					ResponseHelper.getHeaderItems(status, requestPath, action));
			apiResponse.setResponse(aspireObject, "error", ResponseHelper
					.getErrorItems(status, errorCodeStr, errorMessage,
							requestParams));
		}

		message = apiResponse.getResponseOutputString(aspireObject, format,
				pretty);

		AspireObjectUtils.closeAspireObject(aspireObject);

		ResponseEntity<String> entity = new ResponseEntity<String>(message,
				status);
		return entity;

	}

	private LinkedHashMap<String, Object> getContributionTotalResponse(
			ServiceResponseObject responseObject) {
		LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>();

		results.put("totalTags",
				(Integer) responseObject.getContentMap().get("TagTotal"));
		results.put("totalComments", (Integer) responseObject.getContentMap()
				.get("CommentTotal"));
		results.put("totalTranscriptions", (Integer) responseObject
				.getContentMap().get("TranscriptionTotal"));
		results.put("totalModerator", (Integer) responseObject.getContentMap()
				.get("ModeratorTotal"));

		return results;
	}

}
