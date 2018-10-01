package gov.nara.opa.architecture.web.cache;


public class ThreadContext {
  private static final ThreadLocal<String> loggerContext = new ThreadLocal<String>() {
    @Override
    protected String initialValue() {
      return "";
    }
  };
  
  public static String getLoggerContext() {
    return loggerContext.get();
  }
  
  public static void setLoggerContext(String loggerContextValue) {
    loggerContext.set(loggerContextValue);
  }
    
}
