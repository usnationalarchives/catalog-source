package gov.nara.opa.api.controller.authentication;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.authentication.AuthenticationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.user.accounts.UserAccountErrorCode;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.authentication.AuthenticationValidator;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import com.searchtechnologies.aspire.services.AspireObject;

@Controller
public class AuthenticationController {
  
  private static OpaLogger logger = OpaLogger.getLogger(AuthenticationController.class);

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private AuthenticationValidator authenticationValidator;

  @Autowired
  APIResponse apiResponse;

  static Logger log = Logger.getLogger(AuthenticationController.class);

  /**
   * Method to process a user logout request
   * 
   * @param webRequest
   *          Web request instance
   * @param request
   *          Http request
   * @param format
   *          OPA Response Object Format
   * @param pretty
   *          Pretty Print
   * @return Aspire JSON/XML Response Object
   */
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM + "/logout" }, method = RequestMethod.POST)
  public ResponseEntity<String> logout(
      WebRequest webRequest,
      HttpServletRequest request,
      @PathVariable String apiType,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    HttpHeaders responseHeaders = new HttpHeaders();
    HttpStatus status = HttpStatus.OK;
    String action = "logout";
    String resultType = "logout";
    String responseMessage = "";

    try {
      // Get the session ID
      // HttpSession session = request.getSession();

      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getPathFromWebRequest(webRequest);

      
      // Validate parameters
      authenticationValidator.resetValidation();
      
      String[] paramNamesStringArray = { "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, request.getQueryString())) {
        status = HttpStatus.BAD_REQUEST;
        authenticationValidator.setStandardError(UserAccountErrorCode.INVALID_PARAMETER);
        authenticationValidator.setIsValid(false);
      }
      
      
      // Validate the API call type
      if(authenticationValidator.getIsValid()) {
        if (!PathUtils.validateApiType(apiType)) {
          authenticationValidator
              .setStandardError(UserAccountErrorCode.INVALID_API_CALL);
        } else {
          // Validate the input parameters
          authenticationValidator.validateLogin(format);
        }
      }

      // If the input values are valid execute the validaiton method
      if (authenticationValidator.getIsValid()) {

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
          session.invalidate();
        }

        // Set header params
        LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
        headerParams.put("status", String.valueOf(status));
        headerParams.put("time", TimestampUtils.getUtcTimestampString());

        // Set request params
        LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
        requestParams.put("path", requestPath);
        requestParams.put("action", action);
        requestParams.put("format", format);
        requestParams.put("pretty", pretty);
        headerParams.put("request", requestParams);

        // Set the results value
        LinkedHashMap<String, Object> resultValues = new LinkedHashMap<String, Object>();
        resultValues.put("user", "");

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response results
        apiResponse.setResponse(responseObj, resultType, resultValues);

      } else {

        // Set header params
        LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
        headerParams.put("@status", String.valueOf(status));
        headerParams.put("time", TimestampUtils.getUtcTimestampString());

        // Set request params
        LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
        requestParams.put("@path", requestPath);
        requestParams.put("action", action);
        headerParams.put("request", requestParams);

        // Set error params
        LinkedHashMap<String, Object> errorParams = new LinkedHashMap<String, Object>();
        errorParams.put("@code", authenticationValidator.getErrorCode()
            .toString());
        errorParams.put("description", authenticationValidator.getErrorCode()
            .getErrorMessage());
        errorParams.put("format", format);
        errorParams.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorParams);
      }

      // Build response string using the response object
      responseMessage = apiResponse.getResponseOutputString(responseObj,
          format, pretty);

      // Close Aspire response object
      responseObj.close();

    } catch (Exception ae) {
      logger.error(ae.getMessage(), ae);
      throw new OpaRuntimeException(ae);
    }

    ResponseEntity<String> entity = new ResponseEntity<String>(responseMessage,
        responseHeaders, status);
    return entity;
  }

}
