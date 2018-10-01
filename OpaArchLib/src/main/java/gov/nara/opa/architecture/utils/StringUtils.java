package gov.nara.opa.architecture.utils;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author gsofizade
 * @date Apr 14, 2014
 * 
 */
public class StringUtils {
	
	//CUSTOM PATTERNS
	public static final String CUSTOM_9999_YEAR_PATTERN = "<([^>]*?)Date>\\s*<year>9999</year>\\s*<logicalDate>.+?</logicalDate>\\s*</\\1Date>";
	

  /**
   * Replace mupltiple white spaces with a single white space
   * 
   * @param str
   *          String to be formatted
   * @return Formatted string
   */
  public static String replaceMultipleWhiteSpaces(String str) {
    return str.replaceAll("\\s+", " ");
  }

  /**
   * Remove " character from the start & end of a string
   * 
   * @param str
   *          String to be formatted
   * @return Formatted string
   */
  public static String removeStartEndQuotes(String str) {
    if (str.startsWith("\"") && str.endsWith("\"")) {
      str = str.substring(1, str.length());
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }

  /**
   * Method to Remove Mark Ups from Fields
   * 
   * @param str
   *          String to be formatted
   * @return Formatted string
   */
  public static String removeMarkUps(String str) {
    str = str.replaceAll("\\{.*?\\}", "").replace("%7B", "{")
        .replace("%7D", "}");
    return str;
  }

  public static boolean isNullOrEmtpy(String value) {
    return (value == null || value.isEmpty());
  }

  public static LinkedHashMap<String, String> convertStringArrayToLinkedHashMap(
      String[] value) {
    LinkedHashMap<String, String> resMap = new LinkedHashMap<String, String>();
    for (int i = 0; i < value.length; i++) {
      resMap.put(value[i].trim(), "");
    }
    return resMap;
  }
  
  public static String convertMapToJSONString (Object object) {
	  if (object == null) {
		  return null;
	  }
	  ObjectMapper mapper = new ObjectMapper();
	  try {
		  return mapper.writeValueAsString(object);
	  } catch (IOException e) {
	      throw new OpaRuntimeException(e);
	  }
  }
  
  public static String replaceString(String pattern, String source, String replacement) {
	  String result = source.replaceAll(pattern, replacement);
	  
	  return result;
  }

}