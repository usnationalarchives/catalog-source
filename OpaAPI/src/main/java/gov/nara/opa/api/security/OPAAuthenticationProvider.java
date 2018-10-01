package gov.nara.opa.api.security;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.authentication.AuthenticationService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.services.user.accounts.ModifyUserAccountService;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.validation.authentication.AuthenticationValidator;
import gov.nara.opa.api.valueobject.user.accounts.UserAccountValueObjectHelper;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.AbstractLogger;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.cache.ThreadContext;
import gov.nara.opa.architecture.web.cache.UserSessionInfo;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class OPAAuthenticationProvider implements AuthenticationProvider,
    UserSessionInfo, InitializingBean {

  private static OpaLogger logger = OpaLogger
      .getLogger(OPAAuthenticationProvider.class);

  public static final String SPRING_ANONYMOUS_USER_NAME = "anonymousUser";

  @Value("${configFilePath}")
  private String configFilePath;

  @Autowired
  private AuthenticationValidator authenticationValidator;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  UserAccountDao userAccountDao;

  @Autowired
  APIResponse apiResponse;

  @Autowired
  private ModifyUserAccountService userAccountModifier;

  @Override
  public Authentication authenticate(Authentication authentication)
      throws AuthenticationException {
    String userName = authentication.getName();
    String password = (String) authentication.getCredentials();
    String format = "json";
    String errorCode = "";
    String errorMessage = "";

    // Set user name in context
    String provisionalContext = String.format("username=%1$s", userName);
    ThreadContext.setLoggerContext(provisionalContext);
    logger.info(" OPAAuthenticationProvider authentication() START",
        provisionalContext);

    boolean isValid = true;
    try {

      // Validate the login parameters
      authenticationValidator.validateLogin(userName, password, format);

      // Validate the username and password
      UserAccount userAccount = authenticationService.authenticateLogin(
          userName, password);

      // If the login is authenticated, grant authority and return
      // authentication token
      if (userAccount.getErrorCode() == null) {
        // update last_action_ts here, so that auto-disable will ignore the account
        // jdh 2016-08-31
        logger.debug("***** UPDATING LAST ACTION TS");
        userAccount.setLastActionTS(new Timestamp(System.currentTimeMillis()));
        List<GrantedAuthority> AUTHORITIES = new ArrayList<GrantedAuthority>();
        AUTHORITIES.add(new SimpleGrantedAuthority(userAccount
            .getAccountRights()));
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            userName, password, AUTHORITIES);
        token.setDetails(userAccount);

        return token;
      } else {
        isValid = false;
        errorCode = userAccount.getErrorCode().toString();
        errorMessage = userAccount.getErrorMessage();
      }

    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new OpaRuntimeException(e);
    }
    if (!isValid) {
      logger.info("message=Authentication Error ",
          String.format("username=%1$s", userName));
      throw new OpaAuthenticationException(" Authentication Error", errorCode,
          errorMessage);
    } else {
      logger.info("message=Invalid login ",
          String.format("username=%1$s", userName));
      throw new BadCredentialsException("Bad Credentials");
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.equals(authentication);
  }

  public static Integer getAccountIdForLoggedInUser() {
    Authentication auth = SecurityContextHolder.getContext()
        .getAuthentication();
    if (!auth.getName().equals(SPRING_ANONYMOUS_USER_NAME)) {
      return ((UserAccount) auth.getDetails()).getAccountId();
    }
    return null;
  }

  public static String getUserRoleForLoggedInUser() {
    Authentication auth = SecurityContextHolder.getContext()
        .getAuthentication();
    if (!auth.getName().equals(SPRING_ANONYMOUS_USER_NAME)) {
      return ((UserAccount) auth.getDetails()).getAccountRights();
    }
    return null;
  }

  public static String getUserTypeForLoggedInUser() {
    Authentication auth = SecurityContextHolder.getContext()
        .getAuthentication();
    if (!auth.getName().equals(SPRING_ANONYMOUS_USER_NAME)) {
      return ((UserAccount) auth.getDetails()).getAccountType();
    }
    return null;
  }

  public static String getUserNameForLoggedInUser() {
    Authentication auth = SecurityContextHolder.getContext()
        .getAuthentication();
    if (auth == null) {
      return null;
    }
    if (!auth.getName().equals(SPRING_ANONYMOUS_USER_NAME)) {
      return ((UserAccount) auth.getDetails()).getUserName();
    }
    return SPRING_ANONYMOUS_USER_NAME;
  }

  @Override
  public String getUserName() {
    return getUserNameForLoggedInUser();
  }

  public static UserAccountValueObject getAccountValueObjectForLoggedInUser () {
	  Authentication auth = SecurityContextHolder.getContext()
			  .getAuthentication();
	  return UserAccountValueObjectHelper.getAccountValueObject(
			  (UserAccount) auth.getDetails());
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    AbstractLogger.setUserSessionInfo(this);

  }

}
