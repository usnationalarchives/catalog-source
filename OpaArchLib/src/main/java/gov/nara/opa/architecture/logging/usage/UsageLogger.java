package gov.nara.opa.architecture.logging.usage;

import gov.nara.opa.architecture.logging.AbstractLogger;

import org.apache.log4j.Logger;

public class UsageLogger extends AbstractLogger {

  protected UsageLogger(String loggerCategory) {
    super(Logger.getLogger(loggerCategory));
  }

}
