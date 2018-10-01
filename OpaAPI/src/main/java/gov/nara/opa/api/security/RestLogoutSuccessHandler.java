package gov.nara.opa.api.security;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

public class RestLogoutSuccessHandler implements LogoutSuccessHandler {

  private static OpaLogger logger = OpaLogger
      .getLogger(RestLogoutSuccessHandler.class);

  @Autowired
  APIResponse apiResponse;

  @Override
  public void onLogoutSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    String action = "logout";
    String resultType = "logout";
    String requestPath = PathUtils.getServeletPath(request);
    String responseMessage = "";
    String format = "json";
    boolean pretty = true;

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Set response status
      response.setStatus(HttpServletResponse.SC_OK);

      // Set http status
      HttpStatus status = HttpStatus.OK;

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

      // Build response string using the response object
      responseMessage = apiResponse.getResponseOutputString(responseObj,
          format, pretty);

      // Close the Aspire OPA response object
      responseObj.close();

      // Write json to the response body
      response.getWriter().write(responseMessage);
      response.getWriter().flush();
      response.getWriter().close();

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

  }
}
