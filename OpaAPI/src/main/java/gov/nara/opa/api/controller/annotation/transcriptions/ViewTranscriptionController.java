package gov.nara.opa.api.controller.annotation.transcriptions;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionResponseValuesHelper;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.response.ResponseHelper;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.transcriptions.ViewTranscriptionService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.AspireObjectUtils;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.transcriptions.TranscriptionValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;

import java.util.HashMap;
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

@Controller
public class ViewTranscriptionController {

  private OpaLogger logger = OpaLogger
      .getLogger(ViewTranscriptionController.class);

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  private TranscriptionValidator validator;

  @Autowired
  private ResponseHelper responseHelper;

  @Autowired
  private TranscriptionResponseValuesHelper responseValueHelper;

  @Autowired
  private ViewTranscriptionService viewTranscriptionService;

  /**
   * Handles the call for a transcription retrieval
   * 
   * @param request
   * @param naId
   * @param objectId
   * @param format
   * @param pretty
   * @return
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/id/{naId}/objects/{objectId:.+}/transcriptions" }, method = RequestMethod.GET)
  public ResponseEntity<String> getTranscription(
      HttpServletRequest request,
      @PathVariable String apiType,
      @PathVariable String naId,
      @PathVariable String objectId,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
    String message = "";
    HttpStatus status = HttpStatus.OK;
    AspireObject aspireObject = AspireObjectUtils
        .getAspireObject("opaResponse");
    TranscriptionErrorCode transcriptionErrorCode = TranscriptionErrorCode.NONE;
    String requestPath = PathUtils.getServeletPath(request);
    String responseType = "transcription";
    String action = "getTranscription";

    validator.resetValidation();

    LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
    requestParams.put("naId", naId);
    requestParams.put("apiType", apiType);
    requestParams.put("objectId", objectId);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);

    // Validate the parameter names
    String[] paramNamesStringArray = { "format", "pretty" };
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

    requestParams.put("action", action);

    // Get transcription
    if (validator.getIsValid()) {
      // Service call
      ServiceResponseObject responseObject = viewTranscriptionService
          .getFullTranscription(naId, objectId);

      transcriptionErrorCode = (TranscriptionErrorCode) responseObject
          .getErrorCode();

      if (transcriptionErrorCode == TranscriptionErrorCode.NONE) {
        logger.info(String.format(
            "naId=%1$s,objectId=%2$s,message=Retrieving transcription", naId,
            objectId));

        HashMap<String, Object> results = (HashMap<String, Object>) responseObject
            .getContentMap();

        AnnotationLock resultLock = (AnnotationLock) results
            .get("AnnotationLock");
        UserAccount resultUserAccount = (UserAccount) results
            .get("UserAccount");
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
        if (transcriptionErrorCode == TranscriptionErrorCode.NOT_FOUND) {
          // Set lock information
          HashMap<String, Object> results = (HashMap<String, Object>) responseObject
              .getContentMap();

          AnnotationLock resultLock = (AnnotationLock) results
              .get("AnnotationLock");
          UserAccount resultUserAccount = (UserAccount) results
              .get("UserAccount");
          UserAccount lockUserAccount = (UserAccount) results
              .get("LockUserAccount");

          responseValueHelper.Init();
          responseValueHelper.setAnnotationLock(resultLock);
          responseValueHelper.setUserAccount(resultUserAccount);
          responseValueHelper.setLockUserAccount(lockUserAccount);

          status = HttpStatus.NOT_FOUND;
        } else {
          status = HttpStatus.BAD_REQUEST;
        }

        logger
            .info(String
                .format(
                    "naId=%1$s,objectId=%2$s,errorCode=%3$s,errorMessage=%4$s,message=Failed transcription retrieval",
                    naId, objectId, transcriptionErrorCode.toString(),
                    transcriptionErrorCode.getErrorMessage()));
      }

    } else {
      status = HttpStatus.BAD_REQUEST;
      transcriptionErrorCode = validator.getErrorCode();
    }

    // Determine if an error code was generated either by the lock logic or
    // by the transcription logic
    if (transcriptionErrorCode != TranscriptionErrorCode.NONE) {

      String errorCodeStr = transcriptionErrorCode.toString();
      String errorMessage = transcriptionErrorCode.getErrorMessage();

      // Call API response setters for error
      apiResponse.setResponse(aspireObject, "header",
          ResponseHelper.getHeaderItems(status, requestPath, action));

      // Lock information if any
      if (transcriptionErrorCode == TranscriptionErrorCode.NOT_FOUND) {
        apiResponse.setResponse(aspireObject, responseType,
            responseValueHelper.getResponseValues());
      }

      apiResponse.setResponse(aspireObject, "error", ResponseHelper
          .getErrorItems(status, errorCodeStr, errorMessage, requestParams));
    }

    message = apiResponse.getResponseOutputString(aspireObject, format, pretty);

    AspireObjectUtils.closeAspireObject(aspireObject);

    ResponseEntity<String> entity = new ResponseEntity<String>(message, status);
    return entity;
  }
}
