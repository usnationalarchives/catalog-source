package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;

public class Exceptions {
  
  /**
   * Throws a new AspireException that wraps another exception.
   * @param source The instance that is throwing the exception.
   * @param code The exception Code, the full name of the throwing class is prepended.
   * @param cause The root exception.
   * @throws AspireException
   */
  public static void throwAspireException(Object source, String code, Throwable cause) throws AspireException{
    throwAspireException(createAspireExceptionCode(source, code), cause);
  }
  
  public static AspireException createAspireException(Object source, String code, Throwable cause){
    return new AspireException(createAspireExceptionCode(source, code), cause);
  }
  
  public static AspireException createAspireException(String code, Throwable cause){
    return new AspireException(code, cause);
  }

  
  public static void throwAspireException(String code, Throwable cause) throws AspireException{
    throw new AspireException(code, cause);
  }
  
  public static String createAspireExceptionCode(Object instance, String code){
    return instance.getClass().getName().concat(".").concat(code);
  }
}
