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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static OpaLogger logger = OpaLogger
      .getLogger(RestAuthenticationEntryPoint.class);

  @Autowired
  APIResponse apiResponse;

  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response, AuthenticationException ae)
      throws IOException, ServletException {
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      logger
          .error("****** RestAuthenticationEntryPoint WAS CALLED WHEN AUTHENTICATION EXISTED!!!!");
    }
    logger.info("RestAuthenticationEntryPoint was called from: "
        + request.getServletPath());
    HttpHeaders responseHeaders = new HttpHeaders();
    String action = "login";
    String requestPath = PathUtils.getServeletPath(request);
    String responseMessage = "";
    String format = "json";
    boolean pretty = true;

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Set error status
      HttpStatus status = HttpStatus.UNAUTHORIZED;

      // Set response status
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

      // Set the session ID in the response headers
      responseHeaders.set("JSESSIONID", null);

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
      errorParams.put("@code", "INVALID_CREDENTIALS");
      errorParams.put("description",
          "Invalid credentials");
      errorParams.put("format", format);
      errorParams.put("pretty", pretty);

      // Set response header
      apiResponse.setResponse(responseObj, "header", headerParams);

      // Set response error
      apiResponse.setResponse(responseObj, "error", errorParams);

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
