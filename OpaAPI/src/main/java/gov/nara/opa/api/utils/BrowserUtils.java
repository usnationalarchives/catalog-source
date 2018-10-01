package gov.nara.opa.api.utils;

import gov.nara.opa.api.services.HttpHeaderHelper;

import javax.servlet.http.HttpServletRequest;

public class BrowserUtils {
  public static boolean isBrowserIE8(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    
    return userAgent.contains(HttpHeaderHelper.IE8_USER_AGENT);
  }
}
