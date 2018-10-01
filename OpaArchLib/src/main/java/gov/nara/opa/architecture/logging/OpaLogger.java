package gov.nara.opa.architecture.logging;

import gov.nara.opa.architecture.logging.usage.UsageLog4jLevel;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;

import org.apache.log4j.Logger;

public class OpaLogger extends AbstractLogger {

  @SuppressWarnings("rawtypes")
  public static OpaLogger getLogger(Class clazz) {
    return new OpaLogger(clazz);
  }

  @SuppressWarnings("rawtypes")
  private OpaLogger(Class clazz) {
    super(Logger.getLogger(clazz));
  }

  public void usage(String message) {
    usage(UsageLogCode.DEAFULT, message);
  }

  public void usage(UsageLogCode usageCode, String message) {
    usageCode.getLogger().log(UsageLog4jLevel.USAGE, message);
  }

  @SuppressWarnings("rawtypes")
  public void usage(Class controllerClass, ApiTypeLoggingEnum apiType,
      UsageLogCode usageCode, String message) {
    StringBuffer logMessage = new StringBuffer("Controller="
        + controllerClass.getName());
    String apiTypeString = apiType != null ? apiType.toString() : "";
    logMessage.append(" Type=" + apiTypeString + ", ");
    logMessage.append(message);
    usage(usageCode, logMessage.toString());
  }
}
