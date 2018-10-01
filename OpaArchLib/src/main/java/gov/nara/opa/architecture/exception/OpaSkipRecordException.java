package gov.nara.opa.architecture.exception;

/**
 * @author aolaru
 * @date Jun 3, 2014 Common Exception to be used accross the app. Its initial
 *       main purpose is to wrap checked exception that we don't care
 *       propagating up the stack through method signatures and converting the
 *       exception to a runtime exception
 * 
 */
public class OpaSkipRecordException extends RuntimeException {

  /**
   * used for serialization
   */
  private static final long serialVersionUID = 177743655L;

  public OpaSkipRecordException(Throwable exception) {
    super(exception);
  }

  public OpaSkipRecordException(String message) {
    super(message);
  }

}
