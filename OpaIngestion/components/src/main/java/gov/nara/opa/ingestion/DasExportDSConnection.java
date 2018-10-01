/**
 * Copyright Search Technologies 2012
 */
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.scanner.DSConnection;

/**
 * Stores a connection or authentication mechanism requiered by a scanner to authenticate to the repository.
 * 
 * Avoids creating multiple instances of the same authentication object.
 * 
 * @author Rafael Alfaro
 *
 */
public class DasExportDSConnection implements DSConnection {

  /*
   * TODO:
   *    * Add any required property along with its getters and setters
   *        For example:
   * 
   *            String username;
   * 
   *            public String getUsername(){
   *              return username;
   *            }
   * 
   *            public void setUsername(String username){
   *              this.username = username;
   *            }
   *            
   *    * Add the connection object required by the scanner to get connected to the repository along with a getter method
   *        (I.e. NTLMAuthetenticationToken, SPSession, etc).
   */
  
  
  /**
   * Create and open a connection object to the repository  
   */
  @Override
  public void open() {
    /*
     * TODO:
     *    Instantiate the connection object
     */
  }

  /**
   * Closes the connection object 
   */
  @Override
  public void close() {
    /*
     * TODO:
     *    Close the connection object and dispose it.
     */

  }

  /**
   * Indicates whether or not the connection to the repository is open
   * @return true if it's connected, false otherwise
   */
  @Override
  public boolean isConnected() {
    /*
     * TODO:
     *    Verify if the connection is open, return true, false otherwise
     */
    return false;
  }

}
