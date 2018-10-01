package gov.nara.opa.api.controller.moderator;

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
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
 * Retrieves a transcription and additional information for the moderator
 */
@Controller
public class ModeratorViewTranscriptionController {

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
   * Retrieves all transcriptions sorted by version or a specific one if the
   * parameter is provided
   * 
   * @param request
   *          The servlet request
   * @param naId
   *          The transcription NaId
   * @param objectId
   *          The transcription object Id
   * @param version
   *          The transcription version number or 'all' to determine the results
   * @param format
   * @param pretty
   * @return The response entity containing the information of either the single
   *         version requested or all of them for the specified naId and
   *         objectId
   */
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/moderator/transcriptions/id/{naId}/objects/{objectId:.+}" }, method = RequestMethod.GET)
  public ResponseEntity<String> getTranscription(
      HttpServletRequest request,
      @PathVariable String apiType,
      @PathVariable String naId,
      @PathVariable String objectId,
      @RequestParam(value = "version", required = false, defaultValue = "") String version,
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
    requestParams.put("apiType", apiType);
    requestParams.put("naId", naId);
    requestParams.put("objectId", objectId);
    requestParams.put("version", version);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);

    // Validate general params
    
    String[] paramNamesStringArray = { "version", "format", "pretty" };
    LinkedHashMap<String, String> validRequestParameterNames = StringUtils
        .convertStringArrayToLinkedHashMap(paramNamesStringArray);
    if (!ValidationUtils.validateRequestParameterNames(
        validRequestParameterNames, request.getQueryString())) {
      status = HttpStatus.BAD_REQUEST;
      validator.setErrorCode(TranscriptionErrorCode.INVALID_PARAMETER);
      validator.setMessage(ErrorConstants.invalidParameterName);
      validator.setIsValid(false);
    }
    
    
    if (validator.getIsValid()) {
      validator.resetValidation();
      validator.validate(requestParams, false);
    }

    // Add action after validation to avoid general CRUD validation errors
    requestParams.put("action", action);

    // Get transcription
    if (validator.getIsValid()) {

      // Call retrieval method depending on version parameter
      if (!version.equals("all")) {
        transcriptionErrorCode = getSingleVersion(naId, objectId, version,
            status, transcriptionErrorCode, requestPath, aspireObject,
            requestParams, responseType, action);
      } else {
        transcriptionErrorCode = getAllVersions(naId, objectId, status,
            transcriptionErrorCode, requestPath, aspireObject, requestParams,
            responseType, action);
      }

    } else {
      transcriptionErrorCode = validator.getErrorCode();
      // if (transcriptionErrorCode == TranscriptionErrorCode.NOT_API_LOGGED_IN)
      // {
      // status = HttpStatus.UNAUTHORIZED;
      // } else {
      status = HttpStatus.BAD_REQUEST;
      // }
    }

    if (transcriptionErrorCode == TranscriptionErrorCode.NOT_FOUND)
      status = HttpStatus.NOT_FOUND;

    // Determine if an error code was generated either by the lock logic or
    // by the transcription logic
    if (transcriptionErrorCode != TranscriptionErrorCode.NONE) {

      String errorCodeStr = transcriptionErrorCode.toString();
      String errorMessage = transcriptionErrorCode.getErrorMessage();

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
   * Retrieves the information for the specific version of a transcription
   * 
   * @param naId
   * @param objectId
   * @param version
   *          The transcription version
   */
  @SuppressWarnings("unchecked")
  private TranscriptionErrorCode getSingleVersion(String naId, String objectId,
      String version, HttpStatus status,
      TranscriptionErrorCode transcriptionErrorCode, String requestPath,
      AspireObject aspireObject, LinkedHashMap<String, Object> requestParams,
      String responseType, String action) {

    // Service call
    ServiceResponseObject responseObject = viewTranscriptionService
        .getFullTranscription(naId, objectId, Integer.parseInt(version));

    transcriptionErrorCode = (TranscriptionErrorCode) responseObject
        .getErrorCode();
    if (transcriptionErrorCode == TranscriptionErrorCode.NONE) {
      HashMap<String, Object> results = (HashMap<String, Object>) responseObject
          .getContentMap();

      // Get results from content map
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
      if (transcriptionErrorCode == TranscriptionErrorCode.NOT_FOUND) {
        status = HttpStatus.NOT_FOUND;
      } else {
        status = HttpStatus.BAD_REQUEST;
      }
    }
    return transcriptionErrorCode;

  }

  /**
   * Retrieves the information for all versions of a transcription
   * 
   * @param naId
   * @param objectId
   */
  @SuppressWarnings("unchecked")
  private TranscriptionErrorCode getAllVersions(String naId, String objectId,
      HttpStatus status, TranscriptionErrorCode transcriptionErrorCode,
      String requestPath, AspireObject aspireObject,
      LinkedHashMap<String, Object> requestParams, String responseType,
      String action) {
    // Service call
    ServiceResponseObject responseObject = viewTranscriptionService
        .getAllTranscriptionVersions(naId, objectId);

    transcriptionErrorCode = (TranscriptionErrorCode) responseObject
        .getErrorCode();
    if (transcriptionErrorCode == TranscriptionErrorCode.NONE) {

      HashMap<String, Object> results = (HashMap<String, Object>) responseObject
          .getContentMap();

      List<AnnotationLogValueObject> logs = (List<AnnotationLogValueObject>) results.get("Versions");
      if (logs == null || logs.size() == 0) {
        // Empty results
        status = HttpStatus.NOT_FOUND;
        transcriptionErrorCode = TranscriptionErrorCode.NOT_FOUND;
      } else {

        // Call API response setters
        apiResponse.setResponse(aspireObject, "header", ResponseHelper
            .getHeaderItems(status, requestPath, action, requestParams));
        apiResponse.setResponse(aspireObject, responseType,
            getResponseValuesForAllVersions(naId, objectId, logs));
      }

    } else {
      status = HttpStatus.BAD_REQUEST;
      transcriptionErrorCode = validator.getErrorCode();
    }
    return transcriptionErrorCode;
  }

  /**
   * Creates the structured map with the values for all version of a
   * transcription
   * 
   * @param naId
   * @param objectId
   * @param logs
   * @return A linked hash map with the values for a request for all versions
   */
  private LinkedHashMap<String, Object> getResponseValuesForAllVersions(
      String naId, String objectId, List<AnnotationLogValueObject> logs) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

    result.put("@naid", naId);
    result.put("@objectId", objectId);
    result.put("type", "transcription");

    ArrayList<LinkedHashMap<String, Object>> versionList = new ArrayList<LinkedHashMap<String, Object>>();
    if (logs != null) {
      for (AnnotationLogValueObject log : logs) {
        LinkedHashMap<String, Object> versionInfo = new LinkedHashMap<String, Object>();
        versionInfo.put("@num", log.getVersionNum());
        versionInfo.put("@action", log.getAction());
        versionInfo.put("@when", TimestampUtils.getUtcString(log.getLogTS()));

        versionList.add(versionInfo);
      }
    }

    result.put("version", versionList);

    return result;
  }

}
