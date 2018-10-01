package gov.nara.opa.api.utils;

import gov.nara.opa.api.system.logging.APILogger;

import java.util.Collection;

public class DebugUtils {
  private static APILogger log = APILogger.getLogger(DebugUtils.class);

  public static <T> void printList(Collection<T> list) {
    for (T item : list) {
      log.debug("viewUserProfile", item.toString());
    }
  }

  public static void print(String text) {
    log.debug("viewUserProfile", text);

  }

}
