package gov.nara.opa.api.security;

import org.springframework.security.core.AuthenticationException;

public class OpaAuthenticationException extends AuthenticationException {

  private static final long serialVersionUID = 11123L;
  String errorCode;
  String errorDescription;

  public OpaAuthenticationException(String msg, String errorCode,
      String errorDescription) {
    super(msg);
    this.errorCode = errorCode;
    this.errorDescription = errorDescription;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getErrorDescription() {
    return errorDescription;
  }

}
