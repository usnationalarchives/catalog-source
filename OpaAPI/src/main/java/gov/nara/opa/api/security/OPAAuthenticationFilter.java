package gov.nara.opa.api.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.TextEscapeUtils;
import org.springframework.util.Assert;

public class OPAAuthenticationFilter extends
    AbstractAuthenticationProcessingFilter {

  public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
  public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";
  public static final String SPRING_SECURITY_LAST_USERNAME_KEY = "SPRING_SECURITY_LAST_USERNAME";
  private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
  private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
  private boolean postOnly = false;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {
    if (postOnly
        && (!request.getMethod().equals("POST") && !request.getMethod().equals(
            "OPTIONS"))) {
      throw new AuthenticationServiceException(
          "Authentication method not supported: " + request.getMethod());
    }
    String username = obtainUsername(request);
    String password = obtainPassword(request);
    if (username == null) {
      username = "";

    }
    if (password == null) {
      password = "";
    }
    username = username.trim();
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
        username, password);
    // Place the last username attempted into HttpSession for views

    HttpSession session = request.getSession(false);
    if (session != null || getAllowSessionCreation()) {
      request.getSession().setAttribute(SPRING_SECURITY_LAST_USERNAME_KEY,
          TextEscapeUtils.escapeEntities(username));
    }
    // Allow subclasses to set the "details" property
    setDetails(request, authRequest);
    AuthenticationManager manager = this.getAuthenticationManager();
    return manager.authenticate(authRequest);
  }

  public OPAAuthenticationFilter() {
    super("/j_spring_security_check");
  }

  protected String obtainPassword(HttpServletRequest request) {
    return request.getParameter(passwordParameter);
  }

  protected String obtainUsername(HttpServletRequest request) {
    return request.getParameter(usernameParameter);
  }

  protected void setDetails(HttpServletRequest request,
      UsernamePasswordAuthenticationToken authRequest) {
    authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
  }

  public void setUsernameParameter(String usernameParameter) {
    Assert.hasText(usernameParameter,
        "Username parameter must not be empty or null");
    this.usernameParameter = usernameParameter;
  }

  public void setPasswordParameter(String passwordParameter) {
    Assert.hasText(passwordParameter,
        "Password parameter must not be empty or null");
    this.passwordParameter = passwordParameter;
  }

  public void setPostOnly(boolean postOnly) {
    this.postOnly = postOnly;
  }

  public final String getUsernameParameter() {
    return usernameParameter;
  }

  public final String getPasswordParameter() {
    return passwordParameter;
  }
}
