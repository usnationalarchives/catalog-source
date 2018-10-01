package gov.nara.opa.architecture.logging;

import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.cache.UserSessionInfo;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public abstract class AbstractLogger {

  protected Logger logger;

  protected final String messageFormat = "%1$s,%2$s";

  private static UserSessionInfo userSessionInfo;

  public AbstractLogger() {
    super();

  }

  protected String formatMessage(String message) {
    return formatMessage(message, null);
  }

  protected String formatMessage(String message, String context) {

    String localContext;

    if (StringUtils.isNullOrEmtpy(context)) {
      UserSessionInfo userSessionInfo = getUserSessionInfo();
      if (userSessionInfo == null) {
        return message;
      }
      String userName = userSessionInfo.getUserName();
      userName = userName == null || userName.equals("anonymousUser") ? "anonymous"
          : userName;
      localContext = "username=" + userName;
    } else {
      localContext = context;
    }

    return String.format(messageFormat, localContext, message);
  }

  public void log(Level level, Object message) {
    logger.log(level, formatMessage(message.toString()));
  }

  public AbstractLogger(Logger logger) {
    this.logger = logger;
  }

  public void info(Object message, String context) {
    logger.info(formatMessage(message.toString(), context));
  }

  public void info(Object message) {
    logger.info(formatMessage(message.toString()));
  }

  public void info(Object message, Throwable throwable) {
    logger.info(formatMessage(message.toString()), throwable);
  }

  public void info(Object message, HashMap<String, Object> params) {
    logger.info(getLogMessage(formatMessage(message.toString()), params));
  }

  public void fatal(Object message) {
    logger.fatal(formatMessage(message.toString()));
  }

  public void fatal(Object message, Throwable throwable) {
    logger.fatal(formatMessage(message.toString()), throwable);
  }

  public void fatal(Object message, HashMap<String, Object> params) {
    logger.fatal(getLogMessage(formatMessage(message.toString()), params));
  }

  public void error(Object message) {
    if (message != null)
      logger.error(formatMessage(message.toString()));
  }

  public void error(Object message, Throwable throwable) {
    if (message == null) {
      message = "null";
    }
    logger.error(formatMessage(message.toString()), throwable);
  }

  public void error(Object message, HashMap<String, Object> params) {
    logger.error(getLogMessage(formatMessage(message.toString()), params));
  }

  public void warn(Object message) {
    logger.warn(formatMessage(message.toString()));
  }

  public void warn(Object message, Throwable throwable) {
    logger.warn(formatMessage(message.toString()), throwable);
  }

  public void warn(Object message, HashMap<String, Object> params) {
    logger.warn(getLogMessage(formatMessage(message.toString()), params));
  }

  public void debug(Object message) {
    logger.debug(formatMessage(message.toString()));
  }

  public void debug(Object message, Throwable throwable) {
    logger.debug(formatMessage(message.toString()), throwable);
  }

  public void debug(Object message, HashMap<String, Object> params) {
    logger.debug(getLogMessage(formatMessage(message.toString()), params));
  }

  public void trace(Object message) {
    logger.trace(formatMessage(message.toString()));
  }

  public void trace(Object message, Throwable throwable) {
    logger.trace(formatMessage(message.toString()), throwable);
  }

  public void trace(Object message, HashMap<String, Object> params) {
    logger.trace(getLogMessage(formatMessage(message.toString()), params));
  }

  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  private String getLogMessage(Object message, HashMap<String, Object> params) {
    String logMessage = message.toString();
    if (params != null) {
      logMessage += " " + hashMapToKeyValuePairs(params);
    }
    return logMessage;
  }

  private String hashMapToKeyValuePairs(HashMap<String, Object> hashMap) {
    String paramString = "";
    for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
      if (!paramString.isEmpty()) {
        paramString += ",";
      }

      paramString += entry.getKey() + "=" + entry.getValue().toString();
    }
    return paramString;
  }

  public static UserSessionInfo getUserSessionInfo() {
    return userSessionInfo;
  }

  public static void setUserSessionInfo(UserSessionInfo userSessionInfo) {
    AbstractLogger.userSessionInfo = userSessionInfo;
  }
}