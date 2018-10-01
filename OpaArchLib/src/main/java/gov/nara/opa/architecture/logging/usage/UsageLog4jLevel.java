package gov.nara.opa.architecture.logging.usage;

import org.apache.log4j.Level;

public class UsageLog4jLevel extends Level {

  /**
   * 
   */
  private static final long serialVersionUID = -3794327189879121880L;

  public static final int USAGE_INT = INFO_INT + (WARN_INT - INFO_INT) / 2;

  protected UsageLog4jLevel(int level, String levelStr, int syslogEquivalent) {
    super(level, levelStr, syslogEquivalent);
  }

  public static final Level USAGE = new UsageLog4jLevel(USAGE_INT, "USAGE", 10);

  public static Level toLevel(String logArgument) {
    if (logArgument != null && logArgument.toUpperCase().equals("USAGE")) {
      return USAGE;
    }
    return toLevel(logArgument, Level.WARN);
  }

  public static Level toLevel(String logArgument, Level defaultLevel) {
    if (logArgument != null && logArgument.toUpperCase().equals("USAGE")) {
      return USAGE;
    }
    return Level.toLevel(logArgument, defaultLevel);
  }

  public static Level toLevel(int val) {
    if (val == USAGE_INT) {
      return USAGE;
    }
    return toLevel(val, Level.WARN);
  }

  public static Level toLevel(int val, Level defaultLevel) {
    if (val == USAGE_INT) {
      return USAGE;
    }
    return Level.toLevel(val, defaultLevel);
  }
}
