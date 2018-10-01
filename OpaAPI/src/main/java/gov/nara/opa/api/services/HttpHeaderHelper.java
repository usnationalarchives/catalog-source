package gov.nara.opa.api.services;

import gov.nara.opa.architecture.logging.OpaLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpHeaderHelper {
  
  public static final String IE8_USER_AGENT = "MSIE 8.0";
  
  private static OpaLogger logger = OpaLogger.getLogger(HttpHeaderHelper.class);
  
  private static final String MEDIA_PATTERN = ".+/media/.*";
  private static final String IMAGE_TILES_PATTERN = ".+/id/.+/objects/.+/image-tiles";
  private static final String EXPORT_FILES_PATTERN = ".+/exports/auth/files/.*";
  private static final String EXPORT_NON_BULK_FILES_PATTERN = ".+/exports/auth/nbfiles/.*";
  private static final String EXPORT_FILES_NO_AUTH_PATTERN = ".+/exports/noauth/files/.*";
  private static final String EXPORT_NON_BULK_FILES_NO_AUTH_PATTERN = ".+/exports/noauth/nbfiles/.*";
  
  

  public static void setHeader(HttpServletRequest request,
      HttpServletResponse response, String headerName, String headerValue) {
    if(validateHeader(headerName, headerValue, request, response)) {
      response.setHeader(headerName, headerValue);
    }
  }

  private static boolean validateHeader(String headerName, String headerValue,
      HttpServletRequest request, HttpServletResponse response) {

    return validateIE8Downloads(headerName, headerValue, request, response);
  }
  
  private static boolean validateIE8Downloads(String headerName, String headerValue,
      HttpServletRequest request, HttpServletResponse response) {
    
    String userAgent = request.getHeader("User-Agent");
    String requestPath = request.getRequestURI();

    // IE8 downloads
    if (headerName.equals("Pragma") 
        && headerValue.equals("no-cache")
        && userAgent.contains(IE8_USER_AGENT) 
        && (isFilteredPattern(requestPath))
        && request.getMethod().equals("GET")) {
      logger.debug("Denying Pragma:no-cache");
      return false;
    }
    if (headerName.equals("Cache-Control") 
        && (headerValue.contains("no-cache") || headerValue.contains("no-store")) 
        && userAgent.contains(IE8_USER_AGENT) 
        && (isFilteredPattern(requestPath))
        && request.getMethod().equals("GET")) {
      logger.debug("Denying Cache-Control:no-cache;no-store");
      return false;
    }
    
    return true;
  }
  
  private static boolean isFilteredPattern(String requestPath) {
    boolean result = false;
    
    result = requestPath.matches(MEDIA_PATTERN);
    result = result || requestPath.matches(IMAGE_TILES_PATTERN);
    result = result || requestPath.matches(EXPORT_FILES_PATTERN);
    result = result || requestPath.matches(EXPORT_NON_BULK_FILES_PATTERN);
    result = result || requestPath.matches(EXPORT_FILES_NO_AUTH_PATTERN);
    result = result || requestPath.matches(EXPORT_NON_BULK_FILES_NO_AUTH_PATTERN);
    
    return result;
  }

}
