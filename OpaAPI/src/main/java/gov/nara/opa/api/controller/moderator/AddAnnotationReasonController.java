package gov.nara.opa.api.controller.moderator;

import gov.nara.opa.api.moderator.AnnotationReason;
import gov.nara.opa.api.moderator.ModeratorErrorCode;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.moderator.AddAnnotationReasonService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.moderator.ModeratorValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AddAnnotationReasonController {

  private static OpaLogger logger = OpaLogger
      .getLogger(AddAnnotationReasonController.class);

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  private ModeratorValidator moderatorValidator;

  @Autowired
  private AddAnnotationReasonService addAnnotationReasonService;

  /**
   * Capability for a registered user with the moderator privileges to create
   * new annotation reasons
   * 
   * @param request
   *          The HttpServletRequest instance
   * @param text
   *          reason of the reason we are creating
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if the output should be pretty printed. Default is true
   * @return ResponseEntity with the json/xml representation of either the
   *         registered item or any encountered error.
   */
  @RequestMapping(value = "/iapi/v1/moderator/contributions/reasons", method = RequestMethod.POST)
  public ResponseEntity<String> addAnnotationReason(
      HttpServletRequest request,
      @RequestParam(value = "text", required = false, defaultValue = "") String text,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String action = "addAnnotationReason";
    String resultType = "annotationReason";
    AnnotationReason annotationReason = new AnnotationReason();

    // Build the Aspire OPA response object
    AspireObject responseObj = new AspireObject("opaResponse");

    // Get the request path
    String requestPath = PathUtils.getServeletPath(request);

    // Set request params
    LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
    requestParams.put("@path", requestPath);
    requestParams.put("action", action);
    requestParams.put("text", text);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);

    try {

      // Retrieve the user account object
      Authentication auth = SecurityContextHolder.getContext()
          .getAuthentication();
      UserAccount account = (UserAccount) auth.getDetails();
      
      // Validate parameters
      String[] paramNamesStringArray = { "text", "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, request.getQueryString())) {
        status = HttpStatus.BAD_REQUEST;
        moderatorValidator.setErrorCode(ModeratorErrorCode.INVALID_PARAMETER);
        moderatorValidator.setMessage(ErrorConstants.invalidParameterName);
        moderatorValidator.setIsValid(false);
      } else {
        moderatorValidator.validateParameters(requestParams);
      }

      if (moderatorValidator.getIsValid()) {
        if (addAnnotationReasonService.isDuplicateReason(
            account.getAccountId(), text)) {
          moderatorValidator
              .setStandardError(ModeratorErrorCode.DUPLICATE_REASON);
        } else {
          // Access service to insert the new reason
          annotationReason = addAnnotationReasonService.createReason(
              account.getAccountId(), text);

          // Set response values
          if (annotationReason != null) {

            // Set header params
            LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
            headerParams.put("@status", String.valueOf(status));
            headerParams.put("time", TimestampUtils.getUtcTimestampString());

            headerParams.put("request", requestParams);

            // Set result values
            LinkedHashMap<String, Object> resultValues = new LinkedHashMap<String, Object>();
            resultValues.put("@reasonId",
                String.valueOf(annotationReason.getReasonId()));
            resultValues.put("@accountId",
                String.valueOf(annotationReason.getAccountId()));
            resultValues.put("@reason", annotationReason.getReason());
            resultValues.put("@status",
                String.valueOf(annotationReason.getStatus()));
            resultValues.put("@created",
                TimestampUtils.getUtcString(annotationReason.getAddedTs()));

            // Set response header
            apiResponse.setResponse(responseObj, "header", headerParams);

            // Set response results
            apiResponse.setResponse(responseObj, resultType, resultValues);

          } else {
            // Return error if create reason method returns null
            moderatorValidator
                .setStandardError(ModeratorErrorCode.INTERNAL_ERROR);
          }
        }
      }

      if (!moderatorValidator.getIsValid()) {
        // Set error status
        status = HttpStatus.BAD_REQUEST;

        // Set header params
        LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
        headerParams.put("@status", String.valueOf(status));
        headerParams.put("time", TimestampUtils.getUtcTimestampString());

        // Set request params
        LinkedHashMap<String, Object> badRequestParams = new LinkedHashMap<String, Object>();
        badRequestParams.put("@path", requestPath);
        badRequestParams.put("action", action);
        headerParams.put("request", badRequestParams);

        // Set error params
        LinkedHashMap<String, Object> errorValuesMap = new LinkedHashMap<String, Object>();
        errorValuesMap.put("@code", moderatorValidator.getErrorCode()
            .toString());
        errorValuesMap.put("description", moderatorValidator.getErrorCode()
            .getErrorMessage());
        errorValuesMap.put("text", text);
        errorValuesMap.put("format", format);
        errorValuesMap.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorValuesMap);
      }

      // Build response string using the response object
      message = apiResponse
          .getResponseOutputString(responseObj, format, pretty);

      // Close Aspire response object
      responseObj.close();

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    ResponseEntity<String> entity = new ResponseEntity<String>(message, status);
    return entity;
  }

}
