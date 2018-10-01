package gov.nara.opa.architecture.logging.usage;

public enum UsageLogCode {
  DEAFULT("defaultUsage"), 
  ACCOUNT("accountUsage"), 
  TAG("tagUsage"), 
  COMMENT("commentUsage"),
  TRANSCRIPTION("transcriptionUsage"),
  SEARCH("searchUsage"),
  ACCESS("accessUsage"),
  EXPORT("exportUsage"),
  LOG_CLICK("logClickUsage"),
  URL_MAPPING("urlMapping");
  

  private String log4jCategory;
  private UsageLogger usageLogger;

  private UsageLogCode(String log4jCategory) {
    this.log4jCategory = log4jCategory;
    this.usageLogger = new UsageLogger(log4jCategory);
  }

  public String getLog4jCategory() {
    return this.log4jCategory;
  }

  public UsageLogger getLogger() {
    return this.usageLogger;
  }

}
