package gov.nara.opa.api.controller.ingestion;

import gov.nara.opa.api.ingestion.UpdatePageNumErrorCode;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.ingestion.UpdatePageNumService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.ingestion.UpdatePageNumValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
public class UpdatePageNumController {

  private static OpaLogger logger = OpaLogger
      .getLogger(UpdatePageNumController.class);

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  private UpdatePageNumValidator updatePageNumValidator;

  @Autowired
  private UpdatePageNumService updatePageNumService;

  /**
   * Rename a existing list
   * 
   * @param webRequest
   *          The web request instance
   * @param listName
   *          The name of the list to create
   * @param newName
   *          The name of the list to create
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if the output should be pretty printed. Default is true
   * @return ResponseEntity with the json/xml representation of the renamed list
   *         or any encountered error.
   */
  @RequestMapping(value = "/iapi/v1/ingestion/updatepagenumber/id/{naId}/objects/{objectId:.+}", method = RequestMethod.PUT)
  public ResponseEntity<String> updatePageNum(
      WebRequest webRequest,
      @PathVariable String naId,
      @PathVariable String objectId,
      @RequestParam(value = "pageNum", required = false) int pageNum,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String action = "updatePageNum";
    String resultType = "updatePageNum";

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getPathFromWebRequest(webRequest);

      // Create map with parameters received on the request
      LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
      requestParams.put("@path", requestPath);
      requestParams.put("naId", naId);
      requestParams.put("objectId", objectId);
      requestParams.put("pageNum", pageNum);
      requestParams.put("action", action);
      requestParams.put("format", format);
      requestParams.put("pretty", pretty);

      // Set header params
      LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
      headerParams.put("@status", String.valueOf(status));
      headerParams.put("time", TimestampUtils.getUtcTimestampString());

      // Add request params to the response
      headerParams.put("request", requestParams);

      // Validate allowed parameters
      String[] paramNamesStringArray = { "pageNum", "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, webRequest.getParameterNames())) {
        status = HttpStatus.BAD_REQUEST;
        updatePageNumValidator.setErrorCode(UpdatePageNumErrorCode.INVALID_PARAMETER);
        updatePageNumValidator.setMessage(ErrorConstants.invalidParameterName);
        updatePageNumValidator.setIsValid(false);
      } else {
        updatePageNumValidator.validateParameters(requestParams);
      }
      
      
      if (updatePageNumValidator.getIsValid()) {

        String[] resultArray = updatePageNumService
            .updatePageNum(naId, objectId, pageNum).trim().split(",");

        // Set result values
        LinkedHashMap<String, Object> resultValues = new LinkedHashMap<String, Object>();
        resultValues.put("time", TimestampUtils.getUtcTimestampString());
        resultValues.put("TotalLogsUpdated", resultArray[0]);
        resultValues.put("TotalTranscriptionsUpdated", resultArray[1]);
        resultValues.put("TotalTagsUpdated", resultArray[2]);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response results
        apiResponse.setResponse(responseObj, resultType, resultValues);

      } else {

        // Set error status if different from 404
        if (status != HttpStatus.NOT_FOUND)
          status = HttpStatus.BAD_REQUEST;

        // Set request params
        LinkedHashMap<String, Object> badRequestParams = new LinkedHashMap<String, Object>();
        badRequestParams.put("@path", requestPath);
        badRequestParams.put("action", action);
        headerParams.put("request", badRequestParams);

        // Set error params
        LinkedHashMap<String, Object> errorParams = new LinkedHashMap<String, Object>();
        errorParams.put("@code", updatePageNumValidator.getErrorCode()
            .toString());
        errorParams.put("description", updatePageNumValidator.getErrorCode()
            .getErrorMessage());
        errorParams.put("objectId", objectId);
        errorParams.put("naId", naId);
        errorParams.put("format", format);
        errorParams.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorParams);
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
