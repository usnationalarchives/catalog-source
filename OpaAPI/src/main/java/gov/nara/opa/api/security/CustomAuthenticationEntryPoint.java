package gov.nara.opa.api.security;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.*;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private static OpaLogger logger = OpaLogger.getLogger(CustomAuthenticationEntryPoint.class);
  

  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response,
      org.springframework.security.core.AuthenticationException exception)
      throws IOException, ServletException {
    logger.trace("CustomAuthenticationEntryPoint");
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "Unauthorized: Authentication token was either missing or invalid.");
  }

}
