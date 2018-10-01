package gov.nara.opa.api.controller.moderator;

import gov.nara.opa.api.moderator.AnnotationReason;
import gov.nara.opa.api.moderator.ModeratorErrorCode;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.moderator.ViewAnnotationReasonService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.moderator.ModeratorValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewAnnotationReasonController {
  private static OpaLogger logger = OpaLogger
      .getLogger(ViewAnnotationReasonController.class);

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  private ModeratorValidator moderatorValidator;

  @Autowired
  private ViewAnnotationReasonService viewAnnotationReasonService;

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
  @RequestMapping(value = "/iapi/v1/moderator/contributions/reasons", method = RequestMethod.GET)
  public ResponseEntity<String> viewAnnotationReason(
      HttpServletRequest request,
      @RequestParam(value = "text", required = false) String text,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String action = "viewAnnotationReason";
    String resultType = "reason";
    String resultsType = "reasons";

    // Build the Aspire OPA response object
    AspireObject responseObj = new AspireObject("opaResponse");

    // Get the request path
    String requestPath = PathUtils.getServeletPath(request);

    // Set request params
    LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
    requestParams.put("@path", requestPath);
    requestParams.put("action", action);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);

    try {

      // Validate parameters
      String[] paramNamesStringArray = { "text", "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, request.getQueryString())) {
        status = HttpStatus.BAD_REQUEST;
        moderatorValidator.setStandardError(ModeratorErrorCode.INVALID_PARAMETER);
        moderatorValidator.setMessage(ErrorConstants.invalidParameterName);
        moderatorValidator.setIsValid(false);
      } else {
        moderatorValidator.validateParameters(requestParams);
      }

      if (moderatorValidator.getIsValid()) {

        // Retrieve the collection of all the reasons
        List<AnnotationReason> reasons = viewAnnotationReasonService
            .viewAnnotationReasons(0, text);

        if (reasons == null || reasons.size() == 0) {
          status = HttpStatus.NOT_FOUND;
          moderatorValidator
              .setStandardError(ModeratorErrorCode.REASONS_NOT_FOUND);
        } else {

          // Set header params
          LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
          headerParams.put("@status", String.valueOf(status));
          headerParams.put("time", TimestampUtils.getUtcTimestampString());

          // Add all the lists found to the response object
          LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
          ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

          for (AnnotationReason annotationReason : reasons) {
            LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
            resultValuesMap.put("@reasonId",
                String.valueOf(annotationReason.getReasonId()));
            resultValuesMap.put("@reason", annotationReason.getReason());
            resultValuesMap.put("@accountId",
                String.valueOf(annotationReason.getAccountId()));
            resultValuesMap.put("@status",
                String.valueOf(annotationReason.getStatus()));
            resultValuesMap.put("@created",
                TimestampUtils.getUtcString(annotationReason.getAddedTs()));
            resultsValueList.add(resultValuesMap);
          }

          resultsMap.put("total", reasons.size());

          resultsMap.put(resultType, resultsValueList);
          requestParams.put("text", text);
          // Add request params to response
          headerParams.put("request", requestParams);

          // Set response header
          apiResponse.setResponse(responseObj, "header", headerParams);

          // Set response results
          apiResponse.setResponse(responseObj, resultsType, resultsMap);
        }

      }

      if (!moderatorValidator.getIsValid()) {

        // Set error status if different from 404
        if (status != HttpStatus.NOT_FOUND)
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
