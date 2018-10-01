package gov.nara.opa.api.controller.annotation.transcriptions;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.locks.AnnotationLockErrorCode;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionResponseValuesHelper;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.response.ResponseHelper;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.locks.CreateLockService;
import gov.nara.opa.api.services.annotation.locks.DeleteLockService;
import gov.nara.opa.api.services.annotation.locks.ValidateLockService;
import gov.nara.opa.api.services.annotation.transcriptions.CreateTranscriptionService;
import gov.nara.opa.api.services.annotation.transcriptions.ViewTranscriptionService;
import gov.nara.opa.api.services.impl.annotation.transcriptions.TranscriptionHelper;
import gov.nara.opa.api.services.search.SingleSearchRecordRetrieval;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.usagelogging.annotation.transcriptions.TranscriptionsLogger;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.AspireObjectUtils;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.transcriptions.TranscriptionValidator;
import gov.nara.opa.api.validation.annotation.transcriptions.TranscriptionsSearchRequestParameters;
import gov.nara.opa.api.validation.annotation.transcriptions.TranscriptionsSearchValidator;
import gov.nara.opa.api.validation.search.SolrParamsValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.search.ResultTypesValidator;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApiTranscriptionController extends AbstractBaseController {
	private static OpaLogger logger = OpaLogger
			.getLogger(TranscriptionController.class);

	@Autowired
	private APIResponse apiResponse;

	@Autowired
	private TranscriptionValidator validator;

	@Autowired
	private ResponseHelper responseHelper;

	@Autowired
	private TranscriptionResponseValuesHelper responseValueHelper;

	@Autowired
	private CreateLockService createLockService;

	@Autowired
	private ValidateLockService validateLockService;

	@Autowired
	private DeleteLockService deleteLockService;

	@Autowired
	private CreateTranscriptionService createTranscriptionService;

	@Autowired
	private ViewTranscriptionService viewTranscriptionService;

	@Autowired
	private TranscriptionHelper transcriptionHelper;

	@Autowired
	private PageNumberUtils pageNumberUtils;

	@Autowired
	private TranscriptionsSearchValidator transcriptionsSearchValidator;

	public static final String CREATE_TRANSCRIPTIONS_SEARCH_ACTION = "saveAndUnlockThroughSearch";

	@Autowired
	SolrParamsValidator solrParamsValidator;

	@Autowired
	SingleSearchRecordRetrieval searchRecordRetrieval;

	@Autowired
	private ConfigurationService configurationService;

	@RequestMapping(value = { "/" + Constants.PUBLIC_API_PATH + "/"
			+ Constants.API_VERS_NUM
			+ "/id/{naId}/objects/{objectId:.+}/transcriptions" }, method = RequestMethod.PUT)
	public ResponseEntity<String> processTranscription(
			HttpServletRequest request,
			@PathVariable String naId,
			@PathVariable String objectId,
			@RequestParam(value = "text", required = false) String text,
			@RequestParam(value = "pageNum", required = false) String pageNum,
			@RequestParam(value = "format", required = false, defaultValue = "json") String format,
			@RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
		return processTranscriptionInternal(request, naId, objectId, text, pageNum,
				format, pretty, false);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM }, method = RequestMethod.PUT, params = "transcription")
	public ResponseEntity<String> processSearchTranscription(
			@Valid TranscriptionsSearchRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = transcriptionsSearchValidator.validate(
				bindingResult, request, AbstractRequestParameters.PUBLIC_API_TYPE);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_TRANSCRIPTIONS_SEARCH_ACTION);
		}

		requestParameters.setQueryParameters(request.getParameterMap());
		solrParamsValidator.validate(validationResult,
				requestParameters.getQueryParameters());
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_TRANSCRIPTIONS_SEARCH_ACTION);
		}
		try {
			SearchRecordValueObject searchRecord = searchRecordRetrieval
					.getSearchRecord(requestParameters.getQueryParameters());

			if (searchRecord == null
					|| !searchRecord.getResultType().equals("object")) {
				ValidationUtils.setValidationError(validationResult,
						ErrorCodeConstants.SEARCH_RESULTS_NOT_FOUND,
						ErrorConstants.SEARCH_INSERT_ERROR, CREATE_TRANSCRIPTIONS_SEARCH_ACTION,
						HttpStatus.NOT_FOUND);
			}
			String naId = null;
			if (searchRecord.getResultType().equals(
					ResultTypesValidator.RESULT_TYPE_OBJECT) || StringUtils.isNullOrEmtpy(searchRecord.getNaId())) {
				naId = searchRecord.getParentDescriptionNaId();
			} else {
				naId = searchRecord.getNaId();
			}

			String objectId = searchRecord.getObjectId();

			return processTranscriptionInternal(request, naId, objectId,
					requestParameters.getTranscription(), null,
					requestParameters.getFormat(), requestParameters.isPretty(), true);

		} catch (Exception ex) {
			ValidationUtils.setValidationError(validationResult,
					ErrorCodeConstants.SEARCH_RESULTS_NOT_FOUND,
					ErrorConstants.SEARCH_ERROR, CREATE_TRANSCRIPTIONS_SEARCH_ACTION,
					HttpStatus.NOT_FOUND);

			return createErrorResponseEntity(validationResult, request,
					CREATE_TRANSCRIPTIONS_SEARCH_ACTION);
		}
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<String> processTranscriptionInternal(
			HttpServletRequest request, String naId, String objectId, String text,
			String pageNum, String format, boolean pretty, boolean fromSearch) {
		String message = "";
		HttpStatus status = HttpStatus.OK;
		HttpSession session = request.getSession();
		AspireObject aspireObject = AspireObjectUtils
				.getAspireObject("opaResponse");
		AnnotationLockErrorCode lockErrorCode = AnnotationLockErrorCode.NONE;
		TranscriptionErrorCode transcriptionErrorCode = TranscriptionErrorCode.NONE;
		String requestPath = PathUtils.getServeletPath(request);
		String responseType = "transcription";
		String action = "saveAndUnlock";
		String apiType = Constants.PUBLIC_API_PATH;

		int accountId = 0;

		validator.resetValidation();

		// Retrieve the user account object
		UserAccount sessionUser = null;
		try {
			Authentication auth = SecurityContextHolder.getContext()
					.getAuthentication();
			sessionUser = (UserAccount) auth.getDetails();
			accountId = sessionUser.getAccountId();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			validator.setStandardError(TranscriptionErrorCode.NOT_API_LOGGED_IN);
		}

		// If public API call and page number is NULL OR EMPTY - retrieve the
		// page number from the search engine for this naId and objectId
		if (apiType.equals(Constants.PUBLIC_API_PATH)
				&& (StringUtils.isNullOrEmtpy(pageNum) || pageNum.equals("0"))) {
			pageNum = Integer.toString(pageNumberUtils.getPageNumber(apiType, naId,
					objectId));
		}

		LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
		requestParams.put("apiType", apiType);
		requestParams.put("naId", naId);
		requestParams.put("objectId", objectId);
		requestParams.put("accountId", accountId);
		requestParams.put("action", action);
		requestParams.put("pageNum", pageNum);
		requestParams.put("format", format);
		requestParams.put("pretty", pretty);

		// Validate general params
		if (validator.getIsValid()) {
			validator.resetValidation();
			validator.validate(requestParams, false);

			if (validator.getIsValid()) {
				validator.resetValidation();
				validator.validateAccountId(accountId);
			}

			if (validator.getIsValid() && !fromSearch) {
				validator.resetValidation();
				validator.validateUnexpectedParameters(request,
						"action,accountId,objectId,text,pageNum,format,pretty");
			}

			if (validator.getIsValid()) {
				validator.resetValidation();
				validator.validateStringFields("text", text, configurationService.getConfig().getTranscriptionsLength());
			}

			if (validator.getIsValid()) {
				validator.resetValidation();
				validator.validateNaId(naId);
			}

			if (validator.getIsValid()) {
				validator.resetValidation();
				validator.validateObjectIds(objectId, naId);
			}

			if (validator.getIsValid()) {
				validator.resetValidation();
				validator.validateNotEmptyString("pageNum", pageNum);
			}
			if (validator.getIsValid()) {
				validator.resetValidation();
				validator.validatePageNumber(pageNum);
			}

		}

		// Perform transcription save
		if (validator.getIsValid()) {

			// Attempt to lock the item
			ServiceResponseObject responseObject = createTranscriptionService.lock(
					accountId, naId, objectId);
			lockErrorCode = (AnnotationLockErrorCode) responseObject.getErrorCode();

			// If lock was successful proceed
			if (lockErrorCode == AnnotationLockErrorCode.NONE) {
				logger.info(String.format(
						"naId=%1$s,objectId=%2$s,message=Locked transcription", naId,
						objectId));

				// Create transcription instance
				Transcription transcription = transcriptionHelper.createTranscription(
						accountId, naId, objectId, text, pageNum, requestPath);

				responseObject = createTranscriptionService.saveAndUnlock(
						transcription, session.getId(), accountId);

				// Log entry
				TranscriptionsLogger.logTranscription(transcription, this.getClass(),
						apiType, "save");

				transcriptionErrorCode = (TranscriptionErrorCode) responseObject
						.getErrorCode();
				if (transcriptionErrorCode == TranscriptionErrorCode.NONE) {
					logger.info(String.format(
							"naId=%1$s,objectId=%2$s,message=Saved transcription", naId,
							objectId));

					HashMap<String, Object> results = responseObject.getContentMap();
					AnnotationLock resultLock = (AnnotationLock) results
							.get("AnnotationLock");
					UserAccount resultUserAccount = (UserAccount) results
							.get("UserAccount");
					UserAccount lockUserAccount = (UserAccount) results
							.get("LockUserAccount");
					Transcription savedTranscription = (Transcription) results
							.get("Transcription");
					AnnotationLogValueObject resultLog = (AnnotationLogValueObject) results
							.get("AnnotationLog");
					LinkedHashMap<Integer, Transcription> previousTranscriptions = (LinkedHashMap<Integer, Transcription>) results
							.get("TranscriptionsByUser");

					HashMap<Integer, UserAccount> previousContributors = (HashMap<Integer, UserAccount>) results
							.get("UserMap");

					// Initialize response value helper with retrieved items
					responseValueHelper.Init();
					responseValueHelper.setAnnotationLock(resultLock);
					responseValueHelper.setUserAccount(resultUserAccount);
					responseValueHelper.setLockUserAccount(lockUserAccount);
					responseValueHelper.setTranscription(savedTranscription);
					responseValueHelper.setAnnotationLog(resultLog);
					responseValueHelper.setPreviousTranscriptions(previousTranscriptions);
					responseValueHelper.setContributorMap(previousContributors);

					// Call API response setters
					apiResponse.setResponse(aspireObject, "header", ResponseHelper
							.getHeaderItems(status, requestPath, action, requestParams));
					apiResponse.setResponse(aspireObject, responseType,
							responseValueHelper.getResponseValues());
				} else {
					logger
					.info(String
							.format(
									"naId=%1$s,objectId=%2$s,errorCode=%3$s,errorMessage=%4$s,message=Failed transcription save",
									naId, objectId, transcriptionErrorCode.toString(),
									transcriptionErrorCode.getErrorMessage()));
				}

			} else {
				// Unable to lock
				logger
				.info(String
						.format(
								"naId=%1$s,objectId=%2$s,errorCode=%3$s,errorMessage=%4$s,message=Failed transcription save",
								naId, objectId, lockErrorCode.toString(),
								lockErrorCode.getErrorMessage()));
				status = HttpStatus.BAD_REQUEST;
			}

		} else {
			status = HttpStatus.BAD_REQUEST;
			transcriptionErrorCode = validator.getErrorCode();
		}

		// Determine if an error code was generated either by the lock logic or
		// by the transcription logic
		if (lockErrorCode != AnnotationLockErrorCode.NONE
				|| transcriptionErrorCode != TranscriptionErrorCode.NONE) {

			String errorCodeStr = null;
			String errorMessage = null;
			if (lockErrorCode != AnnotationLockErrorCode.NONE) {
				errorCodeStr = lockErrorCode.toString();
				errorMessage = lockErrorCode.getErrorMessage();
			} else {
				errorCodeStr = transcriptionErrorCode.toString();
				errorMessage = transcriptionErrorCode.getErrorMessage();
			}

			if (transcriptionErrorCode == TranscriptionErrorCode.NOT_FOUND) {
				status = HttpStatus.NOT_FOUND;
			} else {
				status = HttpStatus.BAD_REQUEST;
			}

			// Call API response setters for error
			apiResponse.setResponse(aspireObject, "header",
					ResponseHelper.getHeaderItems(status, requestPath, action));
			apiResponse.setResponse(aspireObject, "error", ResponseHelper
					.getErrorItems(status, errorCodeStr, errorMessage, requestParams));
		}

		message = apiResponse.getResponseOutputString(aspireObject, format, pretty);

		AspireObjectUtils.closeAspireObject(aspireObject);

		ResponseEntity<String> entity = new ResponseEntity<String>(message, status);
		return entity;
	}

}
