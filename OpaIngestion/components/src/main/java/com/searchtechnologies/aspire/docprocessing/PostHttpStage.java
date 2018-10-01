/**
 * Copyright Search Technologies 2009
 */
package com.searchtechnologies.aspire.docprocessing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.AXML;
import com.searchtechnologies.aspire.framework.ServletParams;
import com.searchtechnologies.aspire.framework.StageImpl;
import com.searchtechnologies.aspire.framework.Standards;
import com.searchtechnologies.aspire.framework.utilities.DateTimeUtilities;
import com.searchtechnologies.aspire.framework.utilities.StringUtilities;
import com.searchtechnologies.aspire.framework.utilities.XMLUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Batch;
import com.searchtechnologies.aspire.services.ComponentBatch;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.transformers.TransformerFactoryWrapper;
import com.searchtechnologies.aspire.transformers.TransformerWrapper;

/**
 * Aspire Pipeline Processing stage to post xml/Aspire Documents to servers over HTTP.
 *
 * @author Paul Nelson, Javier Mendez, Steve Denny
 */
public class PostHttpStage extends StageImpl {

  //Constants  
  private static final String STAGE_ID = "postHttp";
  public static final String DEFAULT_ENCODING = "UTF-8";  

  //Default values (for Solr).
  private static final String DEFAULT_POST_URL = "http://localhost:8983/solr/update";
  private static final String DEFAULT_OK = "<int name=\"status\">0</int>";
  private static final String DEFAULT_POST_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  private static final String DEFAULT_POST_FOOTER = "";
  
  TransformerFactoryWrapper transformerFactory = new TransformerFactoryWrapper(this);

  /**
   * URL read from the configuration.
   */
  private String configPostUrl = null;

  /**
   * Content-Type header to be sent to server. Ignored when sending multi-part forms.
   */
  
  String contentType = null;
  
  /**
   * Name of the file used for directing debugging information.
   */
  String debugOutFile = null;
  
  /**
   * Flag to determine if the debugOutFile should be used.
   */
  private boolean enableDebugOut = false;
  
  private String debugOutArray[] = null;
  private int debugOutArrayIndex = 0;
  
  /**
   * String message returned by the posting server that tells in the post was successful. 
   */
  private String okayResponse = DEFAULT_OK;

  /**
   * Max number of post retries.
   */
  private int maxTries = 3;
  
  /**
   * Time (milliseconds) to wait between each post retry. 
   */
  private int retryWait = DateTimeUtilities.SECONDS(1);
  
  /**
   * Flag that indicates if this stage should send the document to all configured servers. If set, this will take precedence over deterministic flag.
   */
  private boolean broadcast = false; // Send the document to all configured servers

  /**
   * Flag that indicates if this stage should use deterministic routing with multi-server configurations. If set to false (default), it will use round robin to determinate which server URL to use to post each time.
   */
  private boolean deterministic = false;  // if more than one server, the same doc id will always be sent to the same server
  
  /**
   * Path used to get document ID when deterministic mode is on. 
   */
  private String documentIDPath = "/doc/fetchUrl";
  
  /**
   * Array of server URLs to post.
   */
  private URL serversUrl[] = null; //Note: Removed static modifier from this field as it would prevent multiple postHTTP stages to have their own value.

  /**
   * Flag that indicates if content should be sent as a multipart post.
   */
  private  boolean multipartForm = false;
  
  /**
   * List of parameters for multipart form. 
   */
  private List<NameValuePair> multipartParams = null;
  
  /**
   * Parameter name which holds the content to be posted when using multipart form. 
   */
  private String multipartContentParam = "data";
  
  /**
   *Post header when batching is enabled. Must be a valid XML string. Written at the start of the post.
   */
  private String postHeader = null;
  
  /**
   * Post footer when batching is enabled. Must be a valid XML string. Written at the end of the post.
   */
  private String postFooter = null;

  /**
   * Batch used when normal operation is selected. The methods of the batch are called to post to the URL, but no "real" batchin is done.
   */
  private HttpBatch batchForOne = null;
  
  /**
   * Read timeout - set on the HTTP Connection when reading data.
   */
  int readTimeout = DateTimeUtilities.MINUTES(5);

  /**
   * Connection timeout - set on the HTTP connection.
   */
  int connectionTimeout = DateTimeUtilities.MINUTES(1);

  /**
   * Authentication type - indicates what type of authentication must be used
   */
  String authentication = HttpBatch.AUTH_NONE;
  
  /**
   * Username - for authentication
   */
  private String username = null;
  
  /**
   * Password - for authentication
   */
  private String password = null;

  @Override
  public void process(Job j) throws AspireException {
    //Check if j is on a batch. 
    if(!j.isOnBatch()) {
      //Normal operation. Reuse the same code from batches.
      if(serversUrl.length > 1 && !broadcast) {
        //Only with Round Robin and deterministic operation we need to remember the last URL that was posted.
        batchForOne.processOne(j);
      }
      else {
        //Other operations such as single URL and broadcast can have their own batch reference.
        HttpBatch batchReference = createComponentBatch(j.getBatch());
        batchReference.processOne(j);
        batchReference.setTransformerFactory(transformerFactory);
      }
    }
    else {
      //Get existing component batch object.
      ComponentBatch componentBatchObject;

      synchronized (this) {
        componentBatchObject = j.getComponentBatchObject(this.getName());
        //If null,  then create a new one.
        if(componentBatchObject == null) {
          componentBatchObject = createComponentBatch(j.getBatch());
          ((HttpBatch)componentBatchObject).setTransformerFactory(transformerFactory);
          j.setComponentBatchObject(this.getName(), componentBatchObject);
        }
      }

      //Add job to existing batch.
      componentBatchObject.process(j);
    }
  }


  /**
   * Creates the corresponding component batch to use.
   * 
   * <p> Verifies the configured options of post-http to create the respective component batch (HTTP, routed or broadcast).
   * @return New component batch object.
   * @throws AspireException If component batch object cannot be created.
   */
  public HttpBatch createComponentBatch(Batch batch) throws AspireException {

    HttpBatch batchReference = null;

    //Create the corresponding batch reference instance.
    if(serversUrl.length > 1) {
      if(broadcast) {
        batchReference = new BroadcastingHttpBatch(serversUrl,transformerFactory);
      }
      else {
        RoutedHttpBatch routedBatch = new RoutedHttpBatch(serversUrl,transformerFactory);
        routedBatch.setDeterministic(deterministic);
        routedBatch.setDocumentIDPath(documentIDPath);
        batchReference = routedBatch;
      }
    }
    else {
      batchReference = new HttpBatch();
      batchReference.setPostUrl(serversUrl[0]);
    }

    batchReference = initializeBatchReference(batchReference, postHeader, postFooter);

    //Open the batch reference (open streams to each server URL).
    try {
      batchReference.open();
    }
    catch(AspireException ae) {
      batchReference.close();
      throw ae;
    }
    
    batchReference.setBatch(batch);
    if (batch!=null)
      batch.addData(new AspireObject("server",configPostUrl));
    
    return batchReference;
  }

  /**
   * Set common parameters to batch reference object.
   * 
   * @param batchReference the batch
   * @param postHeader the header to send at the start of the batch
   * @param postFooter the footer to send at the end of the batch
   * @return
   */
  private HttpBatch initializeBatchReference(HttpBatch batchReference, String postHeader, String postFooter) {    
    //Set common properties to batch reference.
    batchReference.setTransformerFactory(transformerFactory);
    batchReference.setMaxTries(maxTries);
    batchReference.setRetryWait(retryWait);
    batchReference.setMultipartForm(multipartForm);
    batchReference.setMultipartParams(multipartParams);
    batchReference.setMultipartContentParam(multipartContentParam);
    batchReference.setOkayResponse(okayResponse);
    batchReference.setPostHttpStage(this);
    
    if(authentication.equals(HttpBatch.AUTH_BASIC)) {
      batchReference.setAuthenticationType(HttpBatch.AUTH_BASIC);
      batchReference.setBasicAuthentication(username, password);
    }

    //Set batching parameters to batch reference.
    batchReference.setPostHeader(postHeader);
    batchReference.setPostFooter(postFooter);

    return batchReference;
  }


  @Override
  public void initialize(Element config) throws AspireException {
    registerServletCommand("reload", "reloadTransform");
    registerServletCommand("test", "testTransform");
    registerServletCommand("closeDebugFiles", "closeDebugFiles");

    contentType = getStringFromConfig(config, "contentType", contentType);

    debugOutFile = getStringFromConfig(config, "debugOutFile", debugOutFile);
    if(debugOutFile != null && debugOutFile.contains(";")) {
      debugOutArray = debugOutFile.split(";");
    }
    boolean enableDebugPresent = getStringFromConfig(config,"enableDebugOut", null) != null;
    
    if (!enableDebugPresent) {
      //if there is no enableDebug present and there is a debugOutFile then it should be enabled
      enableDebugOut = debugOutFile!=null;
    } else {
      enableDebugOut = getBooleanFromConfig(config,"enableDebugOut",true);
      if (enableDebugOut && debugOutFile == null) {
        throw new AspireException(this, "PostHttpStage.enableDebugOut-missing-debugOutFile",
            "There is no debugOutFile specied, if enableDebugOut is enabled debugOutFile must be specified.");
      }
    }
    
    okayResponse = getStringFromConfig(config, "okayResponse", okayResponse);
    maxTries = getIntegerFromConfig(config, "maxTries", maxTries, 1, null);
    retryWait = getIntegerFromConfig(config, "retryWait", retryWait, 0, null);
    deterministic = getBooleanFromConfig(config, "deterministic", deterministic);
    broadcast = getBooleanFromConfig(config, "broadcast", broadcast);
    documentIDPath = getStringFromConfig(config, "idPath", documentIDPath);

    readTimeout = getIntegerFromConfig(config, "readTimeout", readTimeout, 0, null);
    connectionTimeout = getIntegerFromConfig(config, "connectionTimeout", connectionTimeout, 0, null);

    postHeader = getStringFromConfig(config, "postHeader", DEFAULT_POST_HEADER);
    postFooter = getStringFromConfig(config, "postFooter", DEFAULT_POST_FOOTER);
    
    authentication = getStringFromConfig(config, "authentication", authentication);
    
    if(authentication.equals(HttpBatch.AUTH_BASIC)) {
      username = getStringFromConfig(config, "username", null);
      password = getStringFromConfig(config, "password", null);
    }
    
    
    transformerFactory.initialize(config);
    
    Element elem = (config == null) ? null : AXML.get(config, "multipartForm");
    if(elem != null) {
      multipartForm = true;
      
      String contentParam = elem.getAttribute("contentParam");
      if(contentParam != null && contentParam.trim().length() != 0)
        multipartContentParam = contentParam.trim();
      
      List<Element> children = AXML.getChildren(elem);
      multipartParams = new LinkedList<NameValuePair>();
      for(Element child : children) {
        String nodeName = child.getNodeName();
        if(nodeName == null || !nodeName.equals("param"))
          throw new AspireException(this, "PostHttpStage.bad-element-in-multipartParams", 
              "Found element %s inside <multipartParams>. Only <param> is allowed.", nodeName);

        String name = child.getAttribute("name");
        if(name == null || name.trim().length() == 0)
          throw new AspireException(this, "PostHttpStage.multipartParam-missing-name",
              "Multipart parameter name is missing an @name attribute.");
        
        String value = child.getTextContent();
        multipartParams.add(new NameValuePair(name,value));
      }
    }

    //Get post server URLs
    configPostUrl = getStringFromConfig(config, "postUrl", DEFAULT_POST_URL);
    if (!StringUtilities.isEmpty(configPostUrl))
      configPostUrl = XMLUtilities.normalizeWhitespace(getStringFromConfig(config, "postUrl", DEFAULT_POST_URL));

    //Convert the post_url String to an array of actual URL object
    if (configPostUrl == null || configPostUrl.isEmpty()) {
      throw new AspireException(this,"aspire.PostHttpStage.no-url-specified", "The URL(s) for the post command must be specified.");
    }

    serversUrl = processURLconfig(configPostUrl);

    //Initialize a shared instance of component batch for use with Round Robin and deterministic operations.
    if(postHeader.equals(DEFAULT_POST_HEADER) && postFooter.equals(DEFAULT_POST_FOOTER) && serversUrl.length > 1 && !broadcast) {
      batchForOne = createComponentBatch(null);
    }
  }

  
  @Override
  public void close() {
    try {
      transformerFactory.clearPool();
    } catch (AspireException e) {
      error(e, "Failed to clear transformer factory pool");
      // IGNORE - Don't throw exceptions during close, just print them out.
    }
    
    closeAllDebugFiles();
  }
  
  
  
  /**
   * Splits the given url string in to an array of URLS
   * @param strUrl the url to split
   * @return the URL(S)
   * @throws AspireException 
   */
  private URL[] processURLconfig(String strUrl) throws AspireException
  {
    // protect ourselves
    if (StringUtilities.isEmpty(strUrl))
      return null;
    
    // Set up the output variable
    ArrayList<URL> urls = new ArrayList<URL>();
    
    // Loop around the urls
    for(String url:strUrl.split(";"))
    {
      if (!StringUtilities.isEmpty(url))
      {
        // For each url
        url = url.trim();
        debug("Got postURL: %s", url);
        try {
          // Convert to a url type
          URL pUrl = new URL(url);
          
          // Add to the list
          urls.add(pUrl);
        } catch (MalformedURLException e) {
          throw new AspireException(this,"aspire.PostHttpStage.malformed-url", "The URL \"%s\" is malformed", url);
        }
      }
    }
    
    if (urls.size() == 0)
      return null;
    
    // Return the array
    return urls.toArray(new URL[urls.size()]);
  }


  /**
   * Status to return to the ui
   * @return the status
   */
  public AspireObject getStatus() throws AspireException
  {
    AspireObject status = addDerivedStatus(STAGE_ID, super.getStatus());
    
    transformerFactory.addStatus(status);
    
    status.setAttribute("postUrl", StringUtilities.emptyNull(serversUrl[0].toString()));
    status.setAttribute("contentType", StringUtilities.emptyNull(contentType));
    status.setAttribute("debugOutFile", StringUtilities.emptyNull(debugOutFile));
    status.setAttribute("okayResponse", StringUtilities.emptyNull(okayResponse));
    status.setAttribute("maxTries", String.valueOf(maxTries));
    status.setAttribute("retryWait", String.valueOf(retryWait));
    status.setAttribute("deterministic", String.valueOf(deterministic));
    if (deterministic)
      status.setAttribute("idPath", String.valueOf(documentIDPath));
    status.popAll();
    return status;
  }


  /** Servlet command to reload the transform
   */
  public boolean reloadTransform(ServletParams params, AspireObject result) throws AspireException {
    try {
      transformerFactory.reload();
    }
    catch(Exception e) {
      addErrorExceptionToResult(result, e, "Error compiling %s Transform for file %s",
         transformerFactory.getStringType(), transformerFactory.getTransformerFileName()
          );
      return false;
    }
    return true;
  }


  /** Servlet command to close all debug files
   */
  public boolean closeDebugFiles(ServletParams params, AspireObject result) throws AspireException {
    closeAllDebugFiles();
    return true;
  }


  /** Servlet command to test the XSL transform.
   * <p/>
   * The servlet parameters are:<br/>
   * <pre>
   *   *  xsl = The XSL file to use for the transform
   *            (if missing then assume the configured transform file)
   *   *  doc = The XML document to use as input to the transform, OR
   *   *  docFile = The XML document, stored in a file, to use as input to the transform     
   * </pre>
   * @throws IOException 
   */
  @SuppressWarnings("resource")
  public boolean testTransform(ServletParams params, AspireObject result) throws AspireException, IOException {
    String xslFileS = params.getAndCheckEmpty("xsl");
    String docXmlS = params.getAndCheckEmpty("doc");
    String docXmlFileS = params.getAndCheckEmpty("docFile");
    AspireObject doc = new AspireObject(Standards.Basic.DOCUMENT_ROOT);

    try {
      if(docXmlS != null)
        doc = AspireObject.createFromXML(new StringReader(docXmlS));
      else if(docXmlFileS != null) {
        doc = AspireObject.createFromXML(new FileReader(new File(getFilePathFromAspireHome(docXmlFileS))));
      }
      else {
        addErrorToResult(result, "Unable to create a document to transform. This command requires either a 'doc' parameter or a 'docFile' parameter to be non-empty.");
        return false;
      }
    }
    catch(Exception e) {
      addErrorExceptionToResult(result, e, "Error parsing input XML document");
      return false;
    }
    StringWriter sw;
    TransformerWrapper transformer=null;

    try {
      if(xslFileS == null) {
        transformerFactory.clearPool();
        transformer = transformerFactory.newTransformer(false);
        transformer.returnToPool();
      }
      else {
        TransformerFactoryWrapper newFactory = transformerFactory.clone();
        
        // HERE HERE: method in factory to change the XSL file and then get a new transformer
        
        // TODO:  MODIFY THE FOLLOWING TO USE THE TRANSFORMER WRAPPERS
        // NOTE:  INSTEAD OF TRANSFORMING TO A DOM ELEMENT --> TRANSFORM TO A StringWriter()
        //        AND THEN JUST OUTPUT THE RESULTING STRING
        /*URL testUrl = getUrlFromAspireHome(xslFileS);
        ATransformer.clearPool(testUrl);*/
        newFactory.setPostXsl(xslFileS);
        transformer = newFactory.newTransformer(false);
      }
      sw = new StringWriter();
      transformer.transformToWriter(doc, sw);
    }
    catch(Exception e) {
      addErrorExceptionToResult(result, e, "Unable to compile and execute transform");
      return false;
    }

    if(docXmlS == null && docXmlFileS != null)
      result.add("documentIn", doc.toXmlString(AspireObject.PRETTY));
    result.add("transformOut",sw.toString());
    sw.close();
    transformer.returnToPool();

    return true;
  }

  
  /**************************************************/
  /**** MANAGING POOL OF OPEN DEBUG FILE STREAMS ****/
  /**************************************************/
  
  LinkedList<DebugFile> openFiles = new LinkedList<DebugFile>();
  
  void closeAllDebugFiles() {
    synchronized(openFiles) {
      for(DebugFile dbfile : openFiles) {
        try {
          dbfile.os.close();
        } catch (IOException e) {
          error(e, "Error while closing output stream.");
        }
      }
      openFiles.clear();
    }
  }
  
  DebugFile getDebugOut() throws AspireException {
    if(!enableDebugOut || debugOutFile == null) return null;

    // Is there an available open file in the pool?
    synchronized(openFiles) {
      if(openFiles.size() > 0) {
        DebugFile dbf = openFiles.remove(); 
        return dbf;
      }
    }

    String fileName = null;
    if(debugOutArray == null)
      fileName = debugOutFile;
    else {
      int whichFile = 0;
      synchronized(this) {
        whichFile = debugOutArrayIndex++;
      }
      fileName = debugOutArray[whichFile % debugOutArray.length];
    }
    
    int lastDot = fileName.lastIndexOf('.');
    int lastSlash = fileName.lastIndexOf('/');
    if(lastSlash < 0)
      lastSlash = fileName.lastIndexOf('\\');

    String baseFile = fileName;
    String extension = "";
    if(lastDot > lastSlash) {
      baseFile = fileName.substring(0,lastDot);
      extension = fileName.substring(lastDot);
    }

    DebugFile debugFile = null;
    
    // ** Didn't find any available debug file, so open a new one
    for(int i = 0 ; ; i++) {
      String fullName = String.format("%s-%03d%s", baseFile, i, extension);
      File f = new File(fullName);
      if(f.exists()) continue;

      File parentFile = f.getParentFile();
      if(!parentFile.exists()) {
        synchronized(this) {
          if(!parentFile.exists()) {
            if(!parentFile.mkdirs()) {
              throw new AspireException(this, "PostHttpStage.cant-create-debug-dir", 
                  "Unable to create directory %s to hold debug out file.", parentFile.getAbsoluteFile());
            }
          }
        }
      }
      
      OutputStream os;
      try {
        os = new BufferedOutputStream(new FileOutputStream(fullName), 1024*16);
      } catch (FileNotFoundException e) {
        throw new AspireException(this, "PostHttpStage.Cant-open-file", e, "Unable to open Debug Output File %s.", fullName);
      }
      debugFile = new DebugFile(baseFile, extension, os, fullName);

      break;
    }
    
    return debugFile;
  }
  
  void returnDebugOutToPool(DebugFile dbf) {
    synchronized(openFiles) {
      openFiles.add(dbf);
    }
  }
  
  protected void setPostUrl(String url) throws AspireException{
  //Get post server URLs
    configPostUrl = url;
    if (!StringUtilities.isEmpty(configPostUrl))
      configPostUrl = XMLUtilities.normalizeWhitespace(url);
    
    serversUrl = processURLconfig(configPostUrl);
    if (batchForOne!=null)
      batchForOne.close();
    batchForOne = createComponentBatch(null);
  }
  
}
