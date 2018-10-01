package gov.nara.opa.api.controller.moderator;

import gov.nara.opa.api.annotation.TagErrorCode;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.response.ResponseHelper;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.tags.ViewTagService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.tags.TagValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Retrieves a tag and additional information for the moderator
 */
@Controller
public class ModeratorViewTagController {

	private static OpaLogger logger = OpaLogger
			.getLogger(ModeratorViewTagController.class);

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private TagValidator validator;

	@Autowired
	private ResponseHelper responseHelper;

	@Autowired
	private ViewTagService viewTagService;

	/**
	 * Retrieves a tag and additional moderator information
	 * 
	 * @param request
	 *            The servlet request object
	 * @param annotationId
	 *            The tag annotation Id
	 * @param format
	 * @param pretty
	 * @return
	 */
	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/tags/{annotationId}",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
					+ "/moderator/tags/{annotationId}" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewTag(
			HttpServletRequest request,
			@PathVariable int annotationId,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

		return getTag(request, annotationId, format, pretty,
				"viewTagWithObject");
	}

	/**
	 * Method to view a single tag
	 * 
	 * @param request
	 *            Http request
	 * @param naid
	 *            NARA ID
	 * @param objectId
	 *            Object ID
	 * @param tagText
	 *            Tag Annotation
	 * @param format
	 *            OPA Response Object Format
	 * @param pretty
	 *            Pretty Print
	 * @return ResponseEntity
	 */
	private ResponseEntity<String> getTag(HttpServletRequest request,
			int annotationId, String format, boolean pretty, String action) {
		TagErrorCode errorCode = TagErrorCode.NONE;
		HttpStatus status = HttpStatus.OK;
		String resultType = "tag";
		String responseMessage = "";

		try {
			// Build the Aspire OPA response object
			AspireObject aspireObject = new AspireObject("opaResponse");

			// Get the request path
			String requestPath = PathUtils.getServeletPath(request);

			// Set request params
			LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
			requestParams.put("annotationId", annotationId);
			requestParams.put("format", format);
			requestParams.put("pretty", pretty);

			// Validate the input parameters
			validator.setIsValid(true);
			boolean ok = true;
			String[] paramNamesStringArray = { "format", "pretty" };
			LinkedHashMap<String, String> validRequestParameterNames = StringUtils
					.convertStringArrayToLinkedHashMap(paramNamesStringArray);
			if (!ValidationUtils.validateRequestParameterNames(
					validRequestParameterNames, request.getQueryString())) {
				status = HttpStatus.BAD_REQUEST;
				validator.setStandardError(TagErrorCode.INVALID_PARAMETER);
				validator.getErrorCode().setErrorMessage(
						ErrorConstants.invalidParameterName);
				validator.setIsValid(false);
			} else {
				ok = validator.validateFormat(format);
			}

			if (ok) {
				validator.validateAnnotationId(annotationId);
			}

			// Get the tag annotation if the input values are valid
			if (validator.getIsValid()) {

				// Service call
				ServiceResponseObject responseObject = viewTagService
						.viewTagById(annotationId);

				errorCode = (TagErrorCode) responseObject.getErrorCode();
				if (errorCode == TagErrorCode.NONE) {
					// Get result objects
					TagValueObject tag = (TagValueObject) responseObject
							.getContentMap().get("Tag");
					AnnotationLogValueObject log = (AnnotationLogValueObject) responseObject
							.getContentMap().get("AnnotationLog");

					// Build response
					apiResponse.setResponse(aspireObject, "header",
							ResponseHelper.getHeaderItems(status, requestPath,
									action, requestParams));

					// Set response results
					apiResponse.setResponse(aspireObject, resultType,
							getResponseMap(tag, log));

				}
				if (errorCode == TagErrorCode.NO_TAGS_FOUND) {
					status = HttpStatus.NOT_FOUND;
				}

			} else {
				errorCode = validator.getErrorCode();
			}

			// Build response object error
			if (errorCode != TagErrorCode.NONE) {

				// Set error status
				if (status != HttpStatus.NOT_FOUND) {
					status = HttpStatus.BAD_REQUEST;
				}

				String errorCodeStr = errorCode.toString();
				String errorMessage = errorCode.getErrorMessage();

				// Set response header
				// Call API response setters for error
				apiResponse.setResponse(aspireObject, "header", ResponseHelper
						.getHeaderItems(status, requestPath, action));

				apiResponse.setResponse(aspireObject, "error", ResponseHelper
						.getErrorItems(status, errorCodeStr, errorMessage,
								requestParams));
			}

			// Build response string using the response object
			responseMessage = apiResponse.getResponseOutputString(aspireObject,
					format, pretty);

			// Close Aspire response object
			aspireObject.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		}

		ResponseEntity<String> entity = new ResponseEntity<String>(
				responseMessage, status);

		return entity;
	}

	/**
	 * Builds the map object to be printed by the AspireObject for the tag and
	 * log information
	 * 
	 * @param tag
	 *            The tag instance
	 * @param log
	 *            The annotation log instance
	 * @return A linked hash map that the AspireObject will process
	 */
	private LinkedHashMap<String, Object> getResponseMap(TagValueObject tag,
			AnnotationLogValueObject log) {
		// Set result values
		LinkedHashMap<String, Object> resultValues = new LinkedHashMap<String, Object>();
		resultValues.put("@text", tag.getAnnotation());
		resultValues.put("@user", tag.getUserName());
		resultValues.put("@fullName", tag.getFullName());
		resultValues.put("@isNaraStaff", String.valueOf(tag.getIsNaraStaff()));

		// TODO: Get real creation timestamp
		resultValues.put("@created",
				TimestampUtils.getUtcString(tag.getAnnotationTS()));

		resultValues.put("@action", log.getAction());
		resultValues.put("@last_modified",
				TimestampUtils.getUtcString(log.getLogTS()));
		resultValues.put("@reason", log.getReasonId());
		resultValues.put("@notes", log.getNotes());

		return resultValues;
	}

}
