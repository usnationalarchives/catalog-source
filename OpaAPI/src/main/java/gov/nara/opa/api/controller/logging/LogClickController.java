package gov.nara.opa.api.controller.logging;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.response.ResponseHelper;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.utils.AspireObjectUtils;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LogClickController extends AbstractBaseController {

  static OpaLogger log = OpaLogger.getLogger(LogClickController.class);

  @Autowired
  private APIResponse apiResponse;

  @RequestMapping(value = { "/" + Constants.INTERNAL_API_PATH + "/"
      + Constants.API_VERS_NUM + "/logclick" }, method = RequestMethod.POST)
  public ResponseEntity<String> logEntry(
      HttpServletRequest request,
      @RequestParam(value = "url", required = false) String url,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    String errorCodeStr = "NONE";
    String errorMessage = "";
    HttpStatus status = HttpStatus.OK;
    AspireObject aspireObject = AspireObjectUtils
        .getAspireObject("opaResponse");
    String requestPath = PathUtils.getServeletPath(request);
    String action = "logClick";
    String responseType = "logClick";

    LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
    requestParams.put("url", url);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);

    // Validation
    // Validate parameters
    String[] paramNamesStringArray = { "url", "format", "pretty" };
    LinkedHashMap<String, String> validRequestParameterNames = StringUtils
        .convertStringArrayToLinkedHashMap(paramNamesStringArray);
    if (!ValidationUtils.validateRequestParameterNames(
        validRequestParameterNames, request.getQueryString())) {
      status = HttpStatus.BAD_REQUEST;
      errorCodeStr = "INVALID_PARAMETER";
      errorMessage = "Invalid parameter";
    }
    
    
    if (errorCodeStr.equals("NONE") && StringUtils.isNullOrEmtpy(url)) {
      status = HttpStatus.BAD_REQUEST;
      errorCodeStr = "INVALID_PARAMETER";
      errorMessage = "Url value cannot be empty";
    }

    if (errorCodeStr.equals("NONE")) {
      try {
        log.usage(LogClickController.class,
            ApiTypeLoggingEnum.API_TYPE_INTERNAL, UsageLogCode.LOG_CLICK,
            String.format("Source={WebPages},url=%1$s", url));
      } catch (Exception e) {
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        errorCodeStr = "INTERNAL_ERROR";
        errorMessage = e.getMessage();
        log.error(e);
      }
    }

    if (errorCodeStr.equals("NONE")) {

      LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
      result.put("result", "success");

      // Call API response setters
      apiResponse.setResponse(aspireObject, "header", ResponseHelper
          .getHeaderItems(status, requestPath, action, requestParams));
      apiResponse.setResponse(aspireObject, responseType, result);

    } else {

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
