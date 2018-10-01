package gov.nara.opa.api.system.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class APILogger extends Logger {

  private Logger logger;

  @SuppressWarnings("rawtypes")
  public static APILogger getLogger(Class clazz) {
    return new APILogger(clazz);
  }

  @SuppressWarnings("rawtypes")
  private APILogger(Class clazz) {
    super(clazz.toString());
    logger = Logger.getLogger(clazz);
  }

  public void info(String methodName, String message) {
    info(methodName, message, null);
  }

  public void info(String methodName, String message,
      HashMap<String, Object> params) {
    logger.info(getLogMessage(methodName, message, params));
  }

  public void fatal(String methodName, String message) {
    fatal(methodName, message, null);
  }

  public void fatal(String methodName, String message,
      HashMap<String, Object> params) {
    logger.fatal(getLogMessage(methodName, message, params));
  }

  public void error(String methodName, String message) {
    error(methodName, message, null);
  }

  public void error(String methodName, String message,
      HashMap<String, Object> params) {
    logger.error(getLogMessage(methodName, message, params));
  }

  public void warn(String methodName, String message) {
    warn(methodName, message, null);
  }

  public void warn(String methodName, String message,
      HashMap<String, Object> params) {
    logger.warn(getLogMessage(methodName, message, params));
  }

  public void debug(String methodName, String message) {
    debug(methodName, message, null);
  }

  public void debug(String methodName, String message,
      HashMap<String, Object> params) {
    logger.debug(getLogMessage(methodName, message, params));
  }

  public void trace(String methodName, String message) {
    trace(methodName, message, null);
  }

  public void trace(String methodName, String message,
      HashMap<String, Object> params) {
    logger.trace(getLogMessage(methodName, message, params));
  }

  private String getLogMessage(String methodName, String message,
      HashMap<String, Object> params) {
    String logMessage = methodName + " " + message;
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
}
