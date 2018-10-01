package gov.nara.opa.api.utils;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.IOException;

public class AspireObjectUtils {
  
  private static OpaLogger logger = OpaLogger.getLogger(AspireObjectUtils.class);
  
  /**
   * Initializes the aspire object
   * 
   * @return New aspire object
   */
  public static AspireObject getAspireObject(String objectName) {
    return new AspireObject(objectName);
  }

  public static void closeAspireObject(AspireObject aspireObject) {
    try {
      aspireObject.close();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }
  }
}
