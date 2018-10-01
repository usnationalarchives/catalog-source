package gov.nara.opa.api.controller.moderator;

import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.response.ResponseHelper;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.moderator.RemoveTranscriptionService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.AspireObjectUtils;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.annotation.transcriptions.TranscriptionValidator;
import gov.nara.opa.architecture.logging.OpaLogger;

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

/**
 * Removes an active transcription
 */
@Controller
public class RemoveTranscriptionController {
  private static OpaLogger logger = OpaLogger
      .getLogger(RemoveTranscriptionController.class);

  @Autowired
  private RemoveTranscriptionService removeTranscriptionService;

  @Autowired
  private TranscriptionValidator validator;

  @Autowired
  private APIResponse apiResponse;

  /**
   * Handles the transcription removal call
   * 
   * @param request
   * @param naId
   * @param objectId
   * @param versionNumber
   *          The version of the transcription to remove
   * @param reasonId
   * @param notes
   * @param format
   * @param pretty
   * @return
   */
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/moderator/transcriptions/id/{naId}/objects/{objectId:.+}" }, method = RequestMethod.DELETE)
  public ResponseEntity<String> removeTranscription(
      HttpServletRequest request,
      @PathVariable String apiType,
      @PathVariable String naId,
      @PathVariable String objectId,
      @RequestParam(value = "versionNumber", required = false, defaultValue = "-1") int versionNumber,
      @RequestParam(value = "reasonId", required = false, defaultValue = "-1") int reasonId,
      @RequestParam(value = "notes", required = false, defaultValue = "") String notes,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    HttpSession session = request.getSession();
    AspireObject aspireObject = AspireObjectUtils
        .getAspireObject("opaResponse");
    TranscriptionErrorCode transcriptionErrorCode = TranscriptionErrorCode.NONE;
    String requestPath = PathUtils.getServeletPath(request);
    String responseType = "transcription";
    String action = "removeTranscription";

    UserAccount sessionUser = null;
    int accountId = 0;

    validator.resetValidation();

    // TODO: Find out why this is throwing an exceptionand remove the
    // try/catch block
    try {
      // Retrieve the user account object
      Authentication auth = SecurityContextHolder.getContext()
          .getAuthentication();
      sessionUser = (UserAccount) auth.getDetails();
      accountId = sessionUser.getAccountId();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      validator.setStandardError(TranscriptionErrorCode.NOT_API_LOGGED_IN);
    }

    LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
    requestParams.put("apiType", apiType);
    requestParams.put("naId", naId);
    requestParams.put("objectId", objectId);
    requestParams.put("accountId", accountId);
    requestParams.put("reasonId", reasonId);
    requestParams.put("versionNumber", versionNumber);
    requestParams.put("notes", notes);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);

    // Validate general params
    if (validator.getIsValid()) {
      validator.resetValidation();
      validator.validate(requestParams, false);
    }

    // Validate unexpected params
    if (validator.getIsValid()) {
      validator.resetValidation();
      validator.validateUnexpectedParameters(request,
          "naId,objectId,versionNumber,reasonId,notes,format,pretty");
    }

    // Perform removal
    if (validator.getIsValid()) {

      // Call removal service
      ServiceResponseObject responseObject = removeTranscriptionService
          .removeTranscription(naId, objectId, versionNumber, reasonId, notes,
              session.getId(), accountId);

      transcriptionErrorCode = (TranscriptionErrorCode) responseObject
          .getErrorCode();
      if (transcriptionErrorCode == TranscriptionErrorCode.NONE) {

        // Create response
        LinkedHashMap<String, Object> responseValues = new LinkedHashMap<String, Object>();
        responseValues.put("result", "success");

        // Call API response setters
        apiResponse.setResponse(aspireObject, "header", ResponseHelper
            .getHeaderItems(status, requestPath, action, requestParams));
        apiResponse.setResponse(aspireObject, responseType, responseValues);

      } else {
        status = HttpStatus.BAD_REQUEST;
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

}
