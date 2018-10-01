/**
* Copyright Search Technologies 2011
*/

package com.searchtechnologies.aspire.docprocessing;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper class to redirect output to multiples streams to files or other log mechanism.
 * 
 */
public class TeeOutputStream extends OutputStream {
  OutputStream tee = null, out = null;
  
  public TeeOutputStream(OutputStream chainedStream, OutputStream teeStream) {
    out = chainedStream;

    if (teeStream == null)
      tee = System.out;
    else
      tee = teeStream;
  }
  
  /** Writes bytes to both streams.
   */
  @Override
  public void write(byte[] b) throws IOException  {
    out.write(b);
    tee.write(b);
  }
   
  
  /** Writes bytes to both streams.
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b,off,len);
    tee.write(b,off,len);
  }
   
  /** Writes the specified byte to both streams.
   */
  @Override
  public void write(int c) throws IOException {
    out.write(c);
    tee.write(c);
  }
  

  /** Closes the output stream - but not the tee stream. The tee stream must be closed
   * by the calling program.
   */
  @Override
  public void close() throws IOException {
    flush();

    out.close();
    // tee.close(); // Never close the tee stream, must be done by calling program
  }
  

  /**
   * Flushes both the chained and the tee stream.
   */
  @Override
  public void flush() throws IOException {
    out.flush();
    tee.flush();
  }
}