package gov.nara.opa.api.utils;


public class NullableUtils {
  
  public static <T> T ifNullReturnDefault(T checkedValue, T defaultValue) {
    if(checkedValue == null) {
      return defaultValue;
    } else {
      return checkedValue;
    }
  }
  
}
