package gov.nara.opa.architecture.exception;

/**
 * @author aolaru
 * @date Jun 3, 2014 Common Exception to be used accross the app. Its initial
 *       main purpose is to wrap checked exception that we don't care
 *       propagating up the stack through method signatures and converting the
 *       exception to a runtime exception
 *       
 * 
 * ticket date       contributor                comments
 * 84300  2017-07-28 Matthew Mariano            added errorId. The error Id is a guid generated and logged at the earliest
 *                                              caught exception.
 */
public class OpaRuntimeException extends RuntimeException {

  /**
   * used for serialization
   */
  private static final long serialVersionUID = 177743654L;

  /**
   * the guid generated and logged at the earliest caught exception
   */
  private String errorId;


public OpaRuntimeException(Throwable exception) {
    super(exception);
  }

  public OpaRuntimeException(String message) {
    super(message);
  }

  public OpaRuntimeException(String message, Exception ex) {
    super(message, ex);
  }
 
  public OpaRuntimeException(String message, Exception ex, String aErrorId) {
	    super(message, ex);
	    errorId=aErrorId;
	  }
  
	/**
	 * @return the errorId
	 */
	public String getErrorId() {
		return errorId;
	}

	/**
	 * @param errorId - the errorId to set
	 */
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}
}
