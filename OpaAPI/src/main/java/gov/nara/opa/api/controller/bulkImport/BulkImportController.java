package gov.nara.opa.api.controller.bulkImport;

import gov.nara.opa.api.services.bulkImport.BulkImportService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.bulkImport.BulkImportValidator;
import gov.nara.opa.api.validation.bulkImport.CommentImportRequestParameters;
import gov.nara.opa.api.validation.bulkImport.TagImportRequestParameters;
import gov.nara.opa.api.validation.bulkImport.TranscriptionImportRequestParameters;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class BulkImportController extends AbstractBaseController {

	public static final String CREATE_IMPORT_ACTION = "createImport";
	public static final String TAG_IMPORT_PARENT_ENTITY_NAME = "tagImport";
	public static final String TRANSCRIPTION_IMPORT_PARENT_ENTITY_NAME = "transcriptionImport";
	public static final String COMMENT_IMPORT_PARENT_ENTITY_NAME = "commentImport";

	@Autowired
	private BulkImportValidator importValidator;

	@Autowired
	private BulkImportService importService;

	private static OpaLogger log = OpaLogger
			.getLogger(BulkImportController.class);

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/bulk-imports" }, method = RequestMethod.POST, params = { "entity=tag" })
	public ResponseEntity<String> createTagImport(
			@Valid TagImportRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) {

		log.trace("starting tag import");

		ValidationResult validationResult = importValidator.validate(
				bindingResult, request);

		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_IMPORT_ACTION);
		}

		if (StringUtils.isNullOrEmtpy(requestParameters.getSourceFileUrl())) {
			importService.importTags(requestParameters.getApiType(),
					requestParameters.getHttpSessionId(),
					requestParameters.getContent(), validationResult);
		} else {
			importService.importTagsFromFile(requestParameters.getApiType(),
					requestParameters.getHttpSessionId(),
					requestParameters.getSourceFileUrl(), validationResult);
		}

		if (validationResult.isValid()) {
			LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>();
			results.put("results", "success");

			return createSuccessResponseEntity(requestParameters, results,
					request, CREATE_IMPORT_ACTION);
		} else {
			String errorMessage;
			StringBuilder sb = new StringBuilder();
			for (ValidationError error : validationResult.getErrors()) {
				sb.append(error.getErrorMessage() + ".");
			}
			errorMessage = sb.toString();

			return createErrorResponseEntity(errorMessage,
					validationResult.getErrorCode(), requestParameters,
					HttpStatus.BAD_REQUEST, request, CREATE_IMPORT_ACTION);
		}
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/bulk-imports" }, method = RequestMethod.POST, params = { "entity=transcription" })
	public ResponseEntity<String> createTranscriptionImport(
			@Valid TranscriptionImportRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) {

		log.trace("starting transcription import");

		String requestPath = PathUtils.getServeletPath(request);

		ValidationResult validationResult = importValidator.validate(
				bindingResult, request);

		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_IMPORT_ACTION);
		}

		if (StringUtils.isNullOrEmtpy(requestParameters.getSourceFileUrl())) {
			importService.importTranscriptions(requestParameters.getApiType(),
					requestParameters.getHttpSessionId(),
					requestParameters.getContent(), requestPath,
					validationResult);
		} else {
			importService.importTranscriptionsFromFile(
					requestParameters.getApiType(),
					requestParameters.getHttpSessionId(),
					requestParameters.getSourceFileUrl(), requestPath,
					validationResult);
		}

		if (validationResult.isValid()) {
			LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>();
			results.put("results", "success");

			return createSuccessResponseEntity(requestParameters, results,
					request, CREATE_IMPORT_ACTION);
		} else {
			String errorMessage;
			StringBuilder sb = new StringBuilder();
			for (ValidationError error : validationResult.getErrors()) {
				sb.append(error.getErrorMessage() + ".");
			}
			errorMessage = sb.toString();

			return createErrorResponseEntity(errorMessage,
					validationResult.getErrorCode(), requestParameters,
					HttpStatus.BAD_REQUEST, request, CREATE_IMPORT_ACTION);
		}
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/bulk-imports" }, method = RequestMethod.POST, params = { "entity=comment" })
	public ResponseEntity<String> createCommentImport(
			@Valid CommentImportRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) {

		log.trace("starting comment import");

		String requestPath = PathUtils.getServeletPath(request);

		ValidationResult validationResult = importValidator.validate(
				bindingResult, request);

		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_IMPORT_ACTION);
		}

		if (StringUtils.isNullOrEmtpy(requestParameters.getSourceFileUrl())) {
			importService.importComments(requestParameters.getApiType(),
					requestParameters.getHttpSessionId(),
					requestParameters.getContent(), requestPath,
					validationResult);
		} else {
			importService.importCommentsFromFile(
					requestParameters.getApiType(),
					requestParameters.getHttpSessionId(),
					requestParameters.getSourceFileUrl(), requestPath,
					validationResult);
		}

		if (validationResult.isValid()) {
			LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>();
			results.put("results", "success");

			return createSuccessResponseEntity(requestParameters, results,
					request, CREATE_IMPORT_ACTION);
		} else {
			String errorMessage;
			StringBuilder sb = new StringBuilder();
			for (ValidationError error : validationResult.getErrors()) {
				sb.append(error.getErrorMessage() + ".");
			}
			errorMessage = sb.toString();

			return createErrorResponseEntity(errorMessage,
					validationResult.getErrorCode(), requestParameters,
					HttpStatus.BAD_REQUEST, request, CREATE_IMPORT_ACTION);
		}
	}
}
