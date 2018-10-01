package gov.nara.opa.api.services.impl.authentication;

import gov.nara.opa.api.dataaccess.authentication.AuthenticationDao;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.services.authentication.AuthenticationService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Config;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.user.accounts.UserAccountErrorCode;
import gov.nara.opa.api.user.accounts.UserAccountErrorConstants;
import gov.nara.opa.api.utils.PasswordUtils;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationServiceImpl implements AuthenticationService {
  private static OpaLogger logger = OpaLogger
      .getLogger(AuthenticationServiceImpl.class);

  @Value("${configFilePath}")
  private String configFilePath;

  static Logger log = Logger.getLogger(AuthenticationService.class);

  @Autowired
  AuthenticationDao dao;

  @Autowired
  UserAccountDao userAccountDao;

  @Autowired
  private ConfigurationService configurationService;

  @Override
  public UserAccount authenticateLogin(String userName, String pwd) {
    UserAccount userAccount = new UserAccount();
    boolean validPwd = true;

    // Get the config.xml file values
    Config configFileValues = configurationService.getConfig(configFilePath);

    String provisionalContext = String.format("username=%1$s", userName);
    logger.info(" start authenticateLogin():" + userName, provisionalContext);

    try {

      // Validate the userName
      List<UserAccount> list;
      list = dao.select(userName);
      if (list != null && list.size() > 0) {
        userAccount = list.get(0);

        int userAccountLoginAttempts = userAccount.getLoginAttempts();
        Timestamp userAccountLockedOnTS = userAccount.getLockedOn();
        Timestamp userAccountInvalidLoginTS = userAccount
            .getFirstInvalidLogin();

        // Retrieve setting: login atttempts
        int maxLoginAttemptsAllowed = configFileValues.getLoginAttempts();

        // Retrieve setting: total lock minutes, convert to milliseconds
        long timeLockedMS = TimeUnit.MINUTES.toMillis(configFileValues
            .getTimeLocked());

        // Retrieve setting: total minutes before lock reset, convert to
        // milliseconds
        long attemptsTimeMS = TimeUnit.MINUTES.toMillis(configFileValues
            .getAttemptsTime());

        boolean lockedOnTsIsLocked = false;
        if (userAccountLockedOnTS != null) {
          lockedOnTsIsLocked = TimestampUtils.compareTimestampsToDifferential(
              userAccountLockedOnTS, null, timeLockedMS);
        }

        boolean attemptTimeTsIsLocked = false;
        if (userAccountInvalidLoginTS != null) {
          attemptTimeTsIsLocked = TimestampUtils
              .compareTimestampsToDifferential(userAccountInvalidLoginTS, null,
                  attemptsTimeMS);
        }

        // If the lock has expired - reset the lock information
        if (!lockedOnTsIsLocked && !attemptTimeTsIsLocked) {
          userAccountLoginAttempts = 0;
          userAccountLockedOnTS = null;
          userAccountInvalidLoginTS = null;
          userAccount.setLoginAttempts(userAccountLoginAttempts);
          userAccount.setLockedOn(userAccountLockedOnTS);
          userAccount.setFirstInvalidLogin(userAccountInvalidLoginTS);
        }

        // Validate the password if the account is not locked
        logger.info("1. userAccount is null:" + (userAccount == null),
            provisionalContext);
        if (!PasswordUtils.passwordMatches(userAccount.getpWord(), pwd)) {
          validPwd = false;
          // lockedOnTsIsLocked = false;
          userAccount.setErrorCode(UserAccountErrorCode.INVALID_LOGIN);
          userAccount
              .setErrorMessage(UserAccountErrorConstants.invalidCredentials);
        }
        logger.info("2. userAccount is null:" + (userAccount == null),
            provisionalContext);

        // if the account is locked, set the userAccount object to null
        if (lockedOnTsIsLocked) {
          userAccount.setErrorCode(UserAccountErrorCode.ACCOUNT_LOCKED);
          userAccount.setErrorMessage(UserAccountErrorConstants.accountLocked);
        }

        // If the password is valid and the account is not locked:
        // Clear the account lock DB fields
        if (validPwd && !lockedOnTsIsLocked) {
          userAccountDao.clearLoginAttemptsInfo(userName);
        }

        // If the password is NOT valid:
        // Update the account lock DB fields
        if (!validPwd) {
          userAccountLoginAttempts = userAccountLoginAttempts + 1;
          boolean lockAccount = false;
          if (userAccountLoginAttempts >= maxLoginAttemptsAllowed) {
            lockAccount = true;
          }
          userAccountDao.updateLoginAttemptsInfo(userName, lockAccount,
              userAccountLoginAttempts);
        }

      } else {
        userAccount.setErrorCode(UserAccountErrorCode.INVALID_LOGIN);
        userAccount
            .setErrorMessage(UserAccountErrorConstants.invalidCredentials);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      logger.error("Exception in authenticateLogin");
      userAccount.setErrorCode(UserAccountErrorCode.INVALID_LOGIN);
      userAccount.setErrorMessage(UserAccountErrorConstants.invalidCredentials);
    }

    return userAccount;
  }

}
