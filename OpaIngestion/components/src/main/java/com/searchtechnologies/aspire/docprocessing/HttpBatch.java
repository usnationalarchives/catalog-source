/**
 * Copyright Search Technologies 2011
 */
package com.searchtechnologies.aspire.docprocessing;
import gov.nara.opa.ingestion.ExFieldGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import com.searchtechnologies.aspire.framework.AXML;
import com.searchtechnologies.aspire.framework.utilities.Base64Utilities;
import com.searchtechnologies.aspire.framework.utilities.DateTimeUtilities;
import com.searchtechnologies.aspire.framework.utilities.FileUtilities;
import com.searchtechnologies.aspire.framework.utilities.SecurityUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Batch;
import com.searchtechnologies.aspire.services.ComponentBatch;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.transformers.TransformerFactoryWrapper;
import com.searchtechnologies.aspire.transformers.TransformerWrapper;
import gov.nara.opa.ingestion.Components;
import gov.nara.opa.ingestion.DasPathsWhiteList;

public class HttpBatch implements ComponentBatch {

  public static final String AUTH_BASIC = "basic";
  public static final String AUTH_NONE = "none";

  static DasPathsWhiteList dasWhiteList;

  PostHttpStage postHttpStage = null;
  Boolean connectionOpen = false;
  String postHeader = "";
  String postFooter = "";
  URL postUrl = null;

  HttpURLConnection conn = null;
  OutputStream output = null;
  Writer writer = null;

  boolean multipartForm = false;
  String authentication = AUTH_NONE;

  String basicAuthString = null;

  List<NameValuePair> multipartParams = null;
  String multipartContentParam = "data";

  TransformerFactoryWrapper transformerFactory;

  String serverResponse = null;
  String errorResponse = null;
  String okayResponse = null;

  int maxTries = 3;
  int retryWait = DateTimeUtilities.SECONDS(1);

  DebugFile currentDebugFile = null;

  private Batch batch;

  /**
   * Default constructor.
   */
  public HttpBatch() {
  }


  /**
   * Constructor
   * @param postXmlStage The post stage
   * @param config the xml config of the component
   */
  public HttpBatch(PostHttpStage postXmlStage, AXML config) {
    this.postHttpStage = postXmlStage;
    postHeader = config.getText("postHeader");
    postFooter = config.getText("postFooter");
    try {
      this.dasWhiteList = Components.getDasPathsWhiteList(this.postHttpStage);
    } catch(Exception e){
      System.out.println("Could not load DAS White List in HttpBatch");
      e.printStackTrace();
    }
  }


  /** Processes a single, non-batched job from start to finish. Guarantees that close() will be called
   * even when there is an error in the job.
   *
   * @param j The job to be processed.
   * @throws AspireException
   */
  public synchronized void processOne(Job j) throws AspireException {
    boolean processComplete = false;
    try {
      open();

      process(j);
      processComplete = true;

      close();
    }
    finally {
      if(!processComplete) {
        //** We reached here because there was an error in open() or process().
        try {
          close();
        }
        catch(Throwable t) {
          postHttpStage.error(t, "Error trying to close connection.");
        }
      }

      // Errors thrown will continue to be thrown after finally{} completes
    }
  }


  @Override
  public synchronized void open() throws AspireException {
    if(connectionOpen) return;

    connectionOpen = true;

    conn = null;
    output = null;
    try {
      if(postHttpStage.debugOutFile != null) {
        currentDebugFile = postHttpStage.getDebugOut();
      }

      // Open connection to server URL.
      conn = openConnection(postUrl);
      output = conn.getOutputStream();

      if(currentDebugFile != null) {
        writer = new OutputStreamWriter(new TeeOutputStream(output, currentDebugFile.os),
                PostHttpStage.DEFAULT_ENCODING);
      }
      else
        writer = new OutputStreamWriter(output, PostHttpStage.DEFAULT_ENCODING);

      try {
        // Write header to output
        writeHeaderToStream();
      }
      catch (IOException e) {
        int serverResponseCode;
        try {
          serverResponseCode = conn.getResponseCode();
        } catch (IOException e1) {
          String errorMsg = String.format("Failed writing to the output stream \"%s\". The remote server is not accesible or not running. HTTP Response = not available",
                  output);
          writeDebugMsg(e1, errorMsg);
          throw new AspireException(
                  postHttpStage, "aspire.PostXmlStage.failedWritingToTheOutputStream", errorMsg);
        }

        String errorMsg = String.format("Failed writing to the output stream \"%s\". The remote server is not accesible or not running. HTTP Response = %d",
                output, serverResponseCode);

        writeDebugMsg(e, errorMsg);
        throw new AspireException(postHttpStage, "aspire.PostXmlStage.failedWritingToTheOutputStream", errorMsg);
      }
    }
    catch (AspireException e) {
      writeDebugMsg(e, "Unable to open connection to server: " + postUrl);
      throw e;
    }
    catch (IOException e) {
      String errorMsg = String.format("Cannot open connection to server. Details %s.", e.getMessage());
      writeDebugMsg(e, errorMsg);
      throw new AspireException(postHttpStage, "aspire.PostXmlStage.connectionFailed", e, errorMsg);
    }

    this.errorResponse = null;
    this.serverResponse = null;
  }


  @Override
  public void close() throws AspireException {
    try {
      closeConnection();
      if (batch!=null)
        batch.addInfo("Batch successful. Server response: %s",  serverResponse);
    }
    catch(AspireException ae) {
      writeDebugMsg(ae, "Error caught closing batch.");
      throw ae;
    }
    finally {
      if(currentDebugFile != null)
        postHttpStage.returnDebugOutToPool(currentDebugFile);

      currentDebugFile = null;
    }
  }


  @Override
  public void reset() throws AspireException {
    close();
  }


  @Override
  public synchronized void process(Job j) throws AspireException {
    AspireObject doc = j.get();

    AspireException finalExcept = null;

    open();  // Open the connection when the first job is received, does nothing if the connection is already open

    // Try to post on current batch, repeating a number of times
    for (int i = 0; i < maxTries; i++) {
      try {
        try {
          writeDataToStream(doc,j.isOnBatch());
          return;
        } catch (AspireException ae) {
          // Remember the exception and then log it
          finalExcept = ae;
          postHttpStage.error(ae,
                  "Failed to post to server \"%s\" (Attempt %d - Will%s retry)",
                  postUrl.toString(), (i + 1),
                  ((i == (maxTries - 1)) ? " not" : ""));
        }

        // Sleep before retrying (not the last time)
        if (i == (maxTries - 1)) {
          try {
            Thread.sleep(retryWait);
          } catch (InterruptedException e) {
            postHttpStage.warn(e, "Sleep interrupted");
          }
        }
      }
      catch(Throwable t) {
        postHttpStage.error(t, "ERROR trying to write to %s.", postUrl.toString());
      }
    }

    // If we get to here, we've failed, so re-throw the last exception we saw.
    if (finalExcept != null) {
      throw finalExcept;
    }
  }


  /**
   * Open the connection to the server(s) URL. Will open an output stream and
   * store it for later writing.
   *
   * @throws AspireException
   */
  HttpURLConnection openConnection(URL postUrl) throws AspireException {
    AspireException finalExcept = null;
    HttpURLConnection conn = null;

    // Try to open connection, repeating a number of times
    for (int i = 0; i < maxTries; i++) {
      try {
        // Obtain the DNS entry and initialize the connection
        conn = getConnection(postUrl);

        // Open output stream for posting to the server URL
        conn.connect();
        return conn;
      } catch (IOException e) {
        finalExcept = new AspireException(
                postHttpStage,
                "aspire.PostXmlStage.unsucessfulPost",
                e,
                "Open connection to server failed. The remote server was not reachable or was not running");
      } catch (AspireException ae) {
        // Remember the exception and then log it
        finalExcept = ae;
        postHttpStage
                .error(
                        ae,
                        "Failed to open connection to server \"%s\" (Attempt %d - Will%s retry)",
                        postUrl.toString(), (i + 1), ((i == (maxTries - 1)) ? " not"
                                : ""));
      } finally {
        /*if (conn != null)
          conn.disconnect();*/
      }

      // Sleep before retrying (not the last time)
      if (i == (maxTries - 1)) {
        try {
          Thread.sleep(retryWait);
        } catch (InterruptedException e) {
          postHttpStage.warn(e, "Sleep interrupted");
        }
      }
    }

    // If we get to here, we've failed, so re-throw the last exception we saw.
    if (finalExcept != null)
      throw finalExcept;

    return null;
  }


  /**
   * Close the connection to the current post URL.
   *
   * <p> Read the response from the server. TODO: Get intput stream for the response.
   * @throws AspireException
   */
  synchronized void closeConnection() throws AspireException {
    if(!connectionOpen) return;
    try {
      if(output == null || conn == null) return;

      writeFooterToStream(output, postFooter);  // Automatically flushes the stream
      if(currentDebugFile != null) currentDebugFile.os.write((int)'\n');

      serverResponse = getResponseFromServer();
      /*conn.disconnect();*/
      /*conn = null;*/
      postHttpStage.debug("Current batch closed. Server response is %s.", serverResponse);

      /*If the successful message does not appear the operation failed.*/
      if(serverResponse.indexOf(okayResponse) < 0)
      {
        String errorMsg = String.format("Failed to post to remote server. The server returned: \"%s\"", serverResponse == null ? "null" : serverResponse);
        postHttpStage.error(errorMsg);
        throw new AspireException(postHttpStage, "aspire.PostXmlStage.unsucessfulPost",
                "Failed to post to the remote server. The server returned: \"%s\"", serverResponse == null ? "null" : serverResponse);
      }
    }
    catch (IOException e) {
      postHttpStage.error("Failed to get server response. The remote server was not reachable or was not running");
      throw new AspireException("aspire.PostXmlStage.serverNotReachable", e, "Failed to get server response. The remote server was not reachable or was not running");
    }
    finally {
      try {
        if(output != null) { output.close(); output = null; }
        if(conn != null)   { 
          /*conn.disconnect();*/
          if (conn.getInputStream() != null)
            conn.getInputStream().close();
          if (conn.getErrorStream() != null)
            conn.getErrorStream().close();
          conn = null;
        }

        connectionOpen = false;
      } catch (IOException e) {
        postHttpStage.error(e, "Error attempting to close output streams.");
      }
    }
  }

  void writeDebugMsg(Throwable t, String msg) {
    try {
      if(currentDebugFile != null) {
        currentDebugFile.os.write((int)'\n');
        currentDebugFile.os.write(msg.getBytes("UTF-8"));
        if(t != null) {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          t.printStackTrace(pw);
          pw.close();
          currentDebugFile.os.write(sw.toString().getBytes("UTF-8"));
        }
        currentDebugFile.os.write((int)'\n');
      }
    }
    catch(Throwable t2) {
      postHttpStage.error(t2, "Error attempting to write to debug out file.");
    }
  }

  /**
   * Gets the server response. This method should ALWAYS be called after closing
   * the connection's output stream.
   *
   * @return
   * @throws AspireException
   * @throws IOException
   */
  synchronized String getResponseFromServer() throws AspireException, IOException {
    StringWriter sw = new StringWriter();
    // Check the response code
    int serverResponseCode = conn.getResponseCode();
    if (serverResponseCode >= 500 && serverResponseCode <= 599) {
      String respMsg = conn.getResponseMessage();
      throw new AspireException(postHttpStage,
              "aspire.PostXmlStage.serverReturnedFailure",
              "The server returned error code %d (%s) for url %s", serverResponseCode, respMsg == null ? "null" : respMsg,
              postUrl.toString());
    } else if (serverResponseCode != 200 && (serverResponseCode != 201)) {
      String errorResponse = readErrorStream(conn.getErrorStream());
      String respMsg = conn.getResponseMessage();

      // Got some other error from the remote server, such as 404 not found,
      // which is probably a configuration error
      throw new AspireException(
              postHttpStage,
              "aspire.PostXmlStage.genericErrorReturnedByRemoteServer",
              "The server returned error code %d (%s). Review the configuration - the most likely cause is a configuration error (%s). Error response string '%s'.",
              serverResponseCode, respMsg == null ? "null" : respMsg, postUrl.toString(), errorResponse);
    }

    // Read the response from Remote Server
    InputStream in = null;
    postHttpStage.debug("POST-2-XML:  Response = " + serverResponseCode);

    // Get the input stream
    try {
      in = conn.getInputStream();
    } catch (IOException e) {
      errorResponse = readErrorStream(conn.getErrorStream());
      throw new AspireException(postHttpStage,
              "aspire.PostXmlStage.failedInputStream.unsucessfulPost", e,
              "Failed to obtain the input stream \"%s\". HTTP Response = %d", in,
              serverResponseCode);
    }

    // Try reading the input stream
    try {
      Reader reader = new InputStreamReader(in);
      FileUtilities.copyStream(reader, sw);
      reader.close();
    } catch (IOException e) {

      // If the input-stream failed, try reading the error stream
      // The error stream will provide more data on why the remote server is
      // having difficulty
      // (such as error messages and stack traces)
      errorResponse = readErrorStream(conn.getErrorStream());

      throw new AspireException(postHttpStage,
              "aspire.PostXmlStage.failedReadingInputStream.unsucessfulPost", e,
              "Failed to read the input stream \"%s\". HTTP Response = %d.", in,
              serverResponseCode);
    } finally {
      if (in != null)
        in.close();
    }

    return sw.toString();
  }


  /**
   * Gets the authorization string from the username and the password
   *
   * @return Authorization String
   */
  static String getAuthStringEnc(String username, String password) {
    String authString = "";

    try {
      authString = username + ":" + SecurityUtilities.decryptString(password);
    } catch (AspireException e) {
    }
    return "Basic "
            + new String(Base64Utilities.encodeBase64(authString.getBytes()));
  }

  /**
   * Open an HttpURLConnection to an URL. Set the respective connection
   * properties.
   *
   * @param url
   *          URL to open the connection to.
   * @return Connection to the provided URL.
   * @throws AspireException
   */
  HttpURLConnection getConnection(URL url) throws AspireException {
    try {
      HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();

      // Set up connection parameters

      if(authentication.equals(AUTH_BASIC)) {
        conn.setRequestProperty("Authorization", basicAuthString);
      }

      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setConnectTimeout(postHttpStage.connectionTimeout);
      conn.setReadTimeout(postHttpStage.readTimeout);

      if (!multipartForm) {
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);

        conn.setRequestProperty("Content-type", String.format("%s; charset=%s",
                postHttpStage.contentType == null ? transformerFactory.getContentType() : postHttpStage.contentType,
                PostHttpStage.DEFAULT_ENCODING));
        conn.setRequestMethod("POST");
      } else {
        conn.setRequestProperty("Accept-Encoding", "identity");
        conn.setRequestProperty("Connection", "close");
        conn.setRequestProperty("Content-type",
                "multipart/form-data; boundary=<<");
      }

      return conn;

    } catch (UnknownHostException ue) {
      throw new AspireException(postHttpStage,
              "aspire.PostXmlStage.unknown-host", ue,
              "Unknown host for URL \"%s\"", postUrl.toString());
    } catch (IOException ioe) {
      throw new AspireException(postHttpStage,
              "aspire.PostXmlStage.initialize-connection-error", ioe,
              "Unable to initialize connection to URL \"%s\"", postUrl.toString());
    }
  }


  /**
   * Write header to the specified output stream. Flush the stream when done.
   *
   * @param out
   * @param header
   * @param doc
   * @param j
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws AspireException
   */
  void writeHeaderToStream() throws UnsupportedEncodingException, IOException, AspireException {
    // Write the multipart prefix
    if (multipartForm) {
      for (NameValuePair param : multipartParams) {
        writer.write("--<<\r\n");
        writer.write("Content-Disposition: form-data; name=\"" + param.name
                + "\"\r\n\r\n");
        writer.write(param.value);
        writer.write("\r\n");
      }

      // Now, output the content parameter
      writer.write("--<<\r\n");
      writer.write("Content-Disposition: form-data; name=\""
              + multipartContentParam + "\"\r\n");

      writer.write(
              "Content-Type: " +
                      (postHttpStage.contentType == null ? transformerFactory.getContentType() : postHttpStage.contentType) +
                      "\r\n\r\n"
      );
    }
    if (transformerFactory.getPostXslUrl()!=null || transformerFactory.getPostString()!=null){
      writer.write(postHeader);
      writer.flush();
      if(currentDebugFile != null) currentDebugFile.os.write((int)'\n');
    }
  }


  /**
   * Write footer to the specified output stream. Flush the streams when done.
   *
   * @param out
   * @param footer
   * @throws IOException
   */
  void writeFooterToStream(OutputStream out, String footer) throws IOException {
    String multipartSuffix = "\r\n--<<--\r\n";

    writer.write(footer);

    if (multipartForm)
      writer.write(multipartSuffix.toString());

    writer.flush();
    if(currentDebugFile != null) currentDebugFile.os.write((int)'\n');
  }


  /**
   * Write the HTTP data to the specified output stream.
   *
   * @param out
   *          The output stream to write to.
   * @param doc
   *          The AspireDocument to write.
   * @throws IOException
   * @throws AspireException
   */
  void writeDataToStream(AspireObject doc, boolean onBatch)
          throws AspireException {
    TransformerWrapper transformer = null;
    ExFieldGenerator eFG=null;
    try {
      StringWriter sw = new StringWriter();
      transformer = transformerFactory.newTransformer(onBatch);
      transformer.transformToWriter(doc, sw);
      sw.close();
      String transFormedXML = sw.toString();
      eFG = new ExFieldGenerator();
      eFG.processXML(doc.toXmlString());
      String exFieldXML = eFG.getOutput();
      writer.append(transFormedXML.replace("</doc>","").trim());
      if (exFieldXML.length() > 0) {
        writer.append(exFieldXML);
      }
      writer.append("</doc>");
    } catch (Exception e) {
      try{
        log("______________________________________________________________________________________________________________");
        log("ExFieldGenerator FAILED");
        log("Exception is:" +e.getMessage());
        log("BAD XML: ");
        log("\n"+doc.toXmlString()+"\n");
        log("ExFieldGenerator output:\n");
        log(eFG.getOutput());

        log("______________________________________________________________________________________________________________");
      }catch(Exception e2){
        // do nothing
      }
      throw new AspireException(postHttpStage,
              "aspire.PostHttpStage.genericException", e,
              "Transformer exception received. Transformer: %s Transform URL=\"%s\"",
              transformerFactory.getStringType(),
              transformerFactory.getTransformerFileName());
    }

    finally {
      if(transformer != null) transformer.returnToPool();
      try {
        writer.flush();
        if(currentDebugFile != null) currentDebugFile.os.write((int)'\n');
      } catch (IOException e) {
        postHttpStage.error(e, "Error attempting to flush output streams. This error is ignored.");
      }
    }
  }


  /**
   * Read an error stream from the remote site. All exception errors are
   * ignored.
   *
   * @param in
   *          The error stream from the HTTP URL connection.
   */
  String readErrorStream(InputStream in) {
    if (in == null)
      return "(no error message received)";

    StringWriter sw = new StringWriter();
    try {
      FileUtilities.copyStream(new InputStreamReader(in), sw);
    } catch (IOException e) {
      return "(no error message received)";
    }
    try {
      in.close();
    } catch (IOException e) {
      /* ignore */
    }
    String retS = sw.toString();
    if (retS != null)
      return retS;
    else
      return "(no error message received)";
  }


  // Getters and setters
  public URL getPostUrl() {
    return postUrl;
  }


  public void setPostUrl(URL postUrl) {
    this.postUrl = postUrl;
  }

  public void setTransformerFactory(TransformerFactoryWrapper transformerFactory) {
    this.transformerFactory = transformerFactory;
  }

  TransformerFactoryWrapper getTransformerFactory() {
    return transformerFactory;
  }

  public int getMaxTries() {
    return maxTries;
  }


  public void setMaxTries(int maxTries) {
    this.maxTries = maxTries;
  }


  public int getRetryWait() {
    return retryWait;
  }


  public void setRetryWait(int retryWait) {
    this.retryWait = retryWait;
  }


  public boolean isMultipartForm() {
    return multipartForm;
  }


  public void setMultipartForm(boolean multipartForm) {
    this.multipartForm = multipartForm;
  }


  public String getMultipartContentParam() {
    return multipartContentParam;
  }


  public void setMultipartContentParam(String multipartContentParam) {
    this.multipartContentParam = multipartContentParam;
  }


  public List<NameValuePair> getMultipartParams() {
    return multipartParams;
  }


  public void setMultipartParams(List<NameValuePair> multipartParams) {
    this.multipartParams = multipartParams;
  }


  public String getPostHeader() {
    return postHeader;
  }


  public void setPostHeader(String postHeader) {
    this.postHeader = postHeader;
  }


  public String getPostFooter() {
    return postFooter;
  }


  public void setPostFooter(String postFooter) {
    this.postFooter = postFooter;
  }


  public String getOkayResponse() {
    return okayResponse;
  }


  public void setOkayResponse(String okayResponse) {
    this.okayResponse = okayResponse;
  }


  public PostHttpStage getPostXmlStage() {
    return postHttpStage;
  }


  public void setPostHttpStage(PostHttpStage postHttpStage) {
    this.postHttpStage = postHttpStage;
  }


  public String getServerResponse() {
    return serverResponse;
  }

  public boolean isOpen() {
    return connectionOpen;
  }

  public void setBatch(Batch batch) {
    this.batch = batch;
  }

  public void setAuthenticationType(String authentication) {
    this.authentication = authentication;
  }


  public void setBasicAuthentication(String username, String password) {
    this.basicAuthString = getAuthStringEnc(username, password);
  }


  @Override
  public void creatorCloseEvent(Batch batch) throws AspireException {
    // TODO Auto-generated method stub

  }
  private void log(Object o){
    if(currentDebugFile != null){
      String s=""+o;
      byte[] b=s.getBytes();
      try {
        currentDebugFile.os.write(b);
        currentDebugFile.os.flush();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    try {
      System.err.println(o);
      System.err.flush();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}


