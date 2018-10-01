package gov.nara.opa.api.utils;

import gov.nara.opa.api.user.accounts.UserAccount;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SessionUtils {

  public static UserAccount getSessionUser() {
    Authentication auth = SecurityContextHolder.getContext()
        .getAuthentication();
    return auth != null && auth.getDetails() != null
        && auth.getDetails().getClass().equals(UserAccount.class) ? (UserAccount) auth
        .getDetails() : null;
  }
  
  
  

}
