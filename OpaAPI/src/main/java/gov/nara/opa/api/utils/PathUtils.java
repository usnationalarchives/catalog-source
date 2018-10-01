package gov.nara.opa.api.utils;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.WebRequest;

public class PathUtils {

  /**
   * Retrieves the path substring from the full web request
   * 
   * @param webRequest
   *          The web request implementation
   * @return The path substring from the full web request.
   */
  public static String getServeletPath(HttpServletRequest request) {
    // return request.getContextPath() + request.getServletPath();
    return request.getServletPath();
  }

  /**
   * Retrieves the path substring from the full web request
   * 
   * @param webRequest
   *          The web request implementation
   * @return The path substring from the full web request.
   */
  public static String getPathFromWebRequest(WebRequest webRequest) {
    return (webRequest.toString().split("uri=")[1]).split(";")[0];
  }

  /**
   * Validates the API type call (must be equal to opa or api)
   * 
   * @param apiType
   *          API call type
   * @return true/false
   */
  public static boolean validateApiType(String apiType) {
    return (apiType.equalsIgnoreCase("iapi") || apiType.equalsIgnoreCase("api"));
  }

  public static String checkAdditionalParameters(WebRequest request,
      String validParameters) {
    String result = "";

    HashSet<String> validParamSet = toHashSet(validParameters);
    DebugUtils.printList(validParamSet);

    Iterator<String> parameterNames = request.getParameterNames();

    while (parameterNames.hasNext()) {
      String parameterName = parameterNames.next();
      if (!validParamSet.contains(parameterName)) {
        result += (result.length() > 0 ? ", " : "") + parameterName;
      }
    }

    return result;
  }

  public static String checkAdditionalParameters(HttpServletRequest request,
      String validParameters) {
    String result = "";

    HashSet<String> validParamSet = toHashSet(validParameters);

    Enumeration<String> parameterNames = request.getParameterNames();

    while (parameterNames.hasMoreElements()) {
      String parameterName = parameterNames.nextElement();
      if (!validParamSet.contains(parameterName)) {
        result += (result.length() > 0 ? ", " : "") + parameterName;
      }
    }

    return result;
  }

  public static boolean checkAllowedValues(String value, String allowedValues) {
    HashSet<String> allowedValueSet = toHashSet(allowedValues);

    return allowedValueSet.contains(value);
  }

  public static HashSet<String> toHashSet(String commaSeparatedList) {
    HashSet<String> result = new HashSet<String>();

    for (String value : commaSeparatedList.split(",")) {
      result.add(value);
    }

    return result;
  }

}
