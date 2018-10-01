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
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.usagelogging.annotation.transcriptions.TranscriptionsLogger;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.AspireObjectUtils;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.transcriptions.TranscriptionValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class TranscriptionController {
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
  private ConfigurationService configurationService;

  /**
   * Processes all transcription save/lock operations
   * 
   * @param webRequest
   * @param apiType
   * @param naId
   * @param objectId
   * @param action
   *          Allowed values are lock, unlock, saveAndRelock and saveAndUnlock
   * @param text
   * @param accountId
   * @param format
   * @param pretty
   * @return
   */
  @RequestMapping(value = { "/" + Constants.INTERNAL_API_PATH + "/"
      + Constants.API_VERS_NUM
      + "/id/{naId}/objects/{objectId:.+}/transcriptions" }, method = RequestMethod.PUT)
  public ResponseEntity<String> processTranscription(
      HttpServletRequest request,
      @PathVariable String naId,
      @PathVariable String objectId,
      @RequestParam(value = "action", required = false) String action,
      @RequestParam(value = "text", required = false) String text,
      @RequestParam(value = "pageNum", required = false) String pageNum,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
    logger
        .info("Entered the process transcription method with http parameters: "
            + request.getParameterMap());
    String message = "";
    HttpStatus status = HttpStatus.OK;
    HttpSession session = request.getSession();
    AspireObject aspireObject = AspireObjectUtils
        .getAspireObject("opaResponse");
    AnnotationLockErrorCode lockErrorCode = AnnotationLockErrorCode.NONE;
    TranscriptionErrorCode transcriptionErrorCode = TranscriptionErrorCode.NONE;
    String requestPath = PathUtils.getServeletPath(request);
    String responseType = "transcription";
    String apiType = Constants.INTERNAL_API_PATH; // This is temporary

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

    String localPageNum = Integer.toString(pageNumberUtils.getPageNumber(
        apiType, naId, objectId));
    if (localPageNum != "0") {
      if (apiType.equals(Constants.PUBLIC_API_PATH)) {
        // If public API call set
        // page number from the search engine for this naId and objectId
        pageNum = localPageNum;
      }
    } else {
      validator.setErrorCode(TranscriptionErrorCode.INVALID_PARAMETER);
      validator.setMessage(ErrorConstants.INVALID_PAGE_NUMBER);
      validator.setIsValid(false);
    }

    LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
    requestParams.put("apiType", apiType);
    requestParams.put("naId", naId);
    requestParams.put("objectId", objectId);
    requestParams.put("accountId", accountId);
    requestParams.put("action", action);
    requestParams.put("pageNumber", pageNum);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);

    // Validate the parameter names
    String[] paramNamesStringArray = { "action", "text", "pageNum", "format",
        "pretty" };
    LinkedHashMap<String, String> validRequestParameterNames = StringUtils
        .convertStringArrayToLinkedHashMap(paramNamesStringArray);
    if (!ValidationUtils.validateRequestParameterNames(
        validRequestParameterNames, request.getQueryString())) {
      status = HttpStatus.BAD_REQUEST;
      validator.setErrorCode(TranscriptionErrorCode.INVALID_PARAMETER);
      validator.setMessage(ErrorConstants.invalidParameterName);
      validator.setIsValid(false);
    }

    // Validate general params
    if (validator.getIsValid()) {
      validator.resetValidation();
      validator.validate(requestParams, false);
    }

    if (validator.getIsValid()) {
      // Detemine action to execute
      if (action.equals("lock")) {
        performLockValidation(accountId, request);
      } else if (action.equals("unlock")) {
        performUnlockValidation(request);
      } else {
        performSaveValidation(request, text, pageNum, apiType);
      }
    }

    if (validator.getIsValid()) {
      if (action.equals("lock")) {
        // Lock a transcription
        lockErrorCode = processLock(accountId, naId, objectId, action, status,
            lockErrorCode, requestPath, aspireObject, requestParams,
            responseType);
      } else if (action.equals("unlock")) {
        // Unlock a transcription
        lockErrorCode = processUnlock(naId, objectId, action,
            sessionUser.getAccountId(), status, lockErrorCode, requestPath,
            aspireObject, requestParams, responseType);
      } else {
        // Save a transcription an unlock/relock
        transcriptionErrorCode = processSave(naId, objectId, accountId, action,
            text, pageNum, status, session, transcriptionErrorCode,
            requestPath, aspireObject, requestParams, responseType, apiType);
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

  /**
   * Performs parameter validation for Lock operation
   * 
   * @param accountId
   * @param webRequest
   */
  private void performLockValidation(int accountId, HttpServletRequest request) {
    // Validate accountId
    if (validator.getIsValid()) {
      validator.resetValidation();
      validator.validateAccountId(accountId);
    }

    if (validator.getIsValid()) {
      validator.resetValidation();
      validator.validateUnexpectedParameters(request,
          "accountId,action,objectId,format,pretty");
    }
  }

  /**
   * Performs parameter validation for Unlock operation
   * 
   * @param webRequest
   */
  private void performUnlockValidation(HttpServletRequest request) {
    if (validator.getIsValid()) {
      validator.resetValidation();
      validator.validateUnexpectedParameters(request,
          "action,objectId,format,pretty");
    }
  }

  /**
   * Performs parameter validation for both the Save/Relock and Save/Unlock
   * operations
   * 
   * @param webRequest
   */
  private void performSaveValidation(HttpServletRequest request, String text,
      String pageNumber, String apiType) {
    if (validator.getIsValid()) {
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
      validator.validateNotEmptyString("pageNum", pageNumber);
    }
    if (validator.getIsValid()) {
      validator.resetValidation();
      validator.validatePageNumber(pageNumber);
    }

  }

  /**
   * Processes a Lock operation for a transcription.
   * 
   * @param accountId
   * @param naId
   * @param objectId
   */
  @SuppressWarnings("unchecked")
  private AnnotationLockErrorCode processLock(int accountId, String naId,
      String objectId, String action, HttpStatus status,
      AnnotationLockErrorCode lockErrorCode, String requestPath,
      AspireObject aspireObject, LinkedHashMap<String, Object> requestParams,
      String responseType) {

    // Attempt to lock the item
    ServiceResponseObject responseObject = createTranscriptionService.lock(
        accountId, naId, objectId);
    lockErrorCode = (AnnotationLockErrorCode) responseObject.getErrorCode();

    // If lock was successful proceed
    if (lockErrorCode == AnnotationLockErrorCode.NONE) {

      logger.info(String.format(
          "naId=%1$s,objectId=%2$s,message=Locked transcription", naId,
          objectId));

      // Retrieve a transcription if any along with all the related
      // entities
      responseObject = viewTranscriptionService.getFullTranscription(naId,
          objectId);
      HashMap<String, Object> results = responseObject.getContentMap();

      AnnotationLock resultLock = (AnnotationLock) results
          .get("AnnotationLock");
      UserAccount resultUserAccount = (UserAccount) results.get("UserAccount");
      UserAccount lockUserAccount = (UserAccount) results
          .get("LockUserAccount");
      Transcription transcription = (Transcription) results
          .get("Transcription");
      LinkedHashMap<Integer, Transcription> previousTranscriptions = (LinkedHashMap<Integer, Transcription>) results
          .get("TranscriptionsByUser");
      HashMap<Integer, UserAccount> previousContributors = (HashMap<Integer, UserAccount>) results
          .get("UserMap");

      // Initialize response value helper with retrieved items
      responseValueHelper.Init();
      responseValueHelper.setAnnotationLock(resultLock);
      responseValueHelper.setUserAccount(resultUserAccount);
      responseValueHelper.setLockUserAccount(lockUserAccount);
      responseValueHelper.setTranscription(transcription);
      responseValueHelper.setContributorMap(previousContributors);
      responseValueHelper.setPreviousTranscriptions(previousTranscriptions);

      // Call API response setters
      apiResponse.setResponse(aspireObject, "header", ResponseHelper
          .getHeaderItems(status, requestPath, action, requestParams));
      apiResponse.setResponse(aspireObject, responseType,
          responseValueHelper.getResponseValues());

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

    return lockErrorCode;
  }

  /**
   * Unlocks a transcriptions
   * 
   * @param naId
   * @param objectId
   */
  private AnnotationLockErrorCode processUnlock(String naId, String objectId,
      String action, int accountId, HttpStatus status,
      AnnotationLockErrorCode lockErrorCode, String requestPath,
      AspireObject aspireObject, LinkedHashMap<String, Object> requestParams,
      String responseType) {
    // Perform unlock
    ServiceResponseObject responseObject = deleteLockService.delete(naId,
        objectId, null, accountId);

    lockErrorCode = (AnnotationLockErrorCode) responseObject.getErrorCode();
    if (lockErrorCode == AnnotationLockErrorCode.NONE) {

      logger.info(String.format(
          "naId=%1$s,objectId=%2$s,message=Unlocked transcription", naId,
          objectId));

      LinkedHashMap<String, Object> responseValues = new LinkedHashMap<String, Object>();
      responseValues.put("result", "success");

      // Call API response setters
      apiResponse.setResponse(aspireObject, "header", ResponseHelper
          .getHeaderItems(status, requestPath, action, requestParams));
      apiResponse.setResponse(aspireObject, responseType, responseValues);
    } else {
      logger
          .info(String
              .format(
                  "naId=%1$s,objectId=%2$s,errorCode=%3$s,errorMessage=%4$s,message=Failed unlock",
                  naId, objectId, lockErrorCode.toString(),
                  lockErrorCode.getErrorMessage()));
    }

    return lockErrorCode;
  }

  /**
   * Validates that a lock is in place for the item and then does a call to
   * transcription save. Determines if the lock should be released or kept and
   * acts accordingly.
   * 
   * @param naId
   * @param objectId
   * @param accountId
   * @param action
   * @param text
   */
  @SuppressWarnings("unchecked")
  private TranscriptionErrorCode processSave(String naId, String objectId,
      int accountId, String action, String text, String pageNumber,
      HttpStatus status, HttpSession session,
      TranscriptionErrorCode transcriptionErrorCode, String requestPath,
      AspireObject aspireObject, LinkedHashMap<String, Object> requestParams,
      String responseType, String apiType) {

    // Validate lock exists
    if (validateLockService.validateLock(accountId, naId, objectId, null)) {

      // Create transcription instance
      Transcription transcription = transcriptionHelper.createTranscription(
          accountId, naId, objectId, text, pageNumber, requestPath);

      ServiceResponseObject responseObject;
      // Perform call according to requested action
      if (action.equals("saveAndRelock")) {
        responseObject = createTranscriptionService.saveAndRelock(
            transcription, session.getId(), accountId);
      } else {
        responseObject = createTranscriptionService.saveAndUnlock(
            transcription, session.getId(), accountId);
      }

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
        AnnotationLogValueObject resultLog = (AnnotationLogValueObject) results.get("AnnotationLog");
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
      // Lock was invalid or already locked

      validator.setStandardError(TranscriptionErrorCode.NO_LOCK);
      transcriptionErrorCode = validator.getErrorCode();
    }

    return transcriptionErrorCode;
  }

}
