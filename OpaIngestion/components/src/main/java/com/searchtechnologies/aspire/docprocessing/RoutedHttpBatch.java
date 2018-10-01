/**
* Copyright Search Technologies 2011
*/
package com.searchtechnologies.aspire.docprocessing;

import java.net.URL;

import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.framework.Standards;
import com.searchtechnologies.aspire.framework.utilities.StringUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.transformers.TransformerFactoryWrapper;

public class RoutedHttpBatch extends MultiServerHttpBatch{

  /**
   * Flag that indicates if this stage should use deterministic routing with multi-server configurations. If set to false (default), it will use round robin to determinate which server URL to use to post each time.
   */
  boolean deterministic = false;  // if more than one server, the same doc id will always be sent to the same server
  
  /**
   * Path used to get document ID when deterministic mode is on. 
   */
  String documentIDPath = "/" + Standards.Basic.DOCUMENT_ROOT + "/" + Standards.Basic.FETCH_URL_TAG;
  
  /**
   * Index to the next URL server used when round robin is on (multiple URLs configured, but deterministic and broadcast flags are off).
   */
  int nextServerUrlIndex = 0; //Note: Removed static modifier from this field as it would prevent multiple postXML stages to have their own 


  public RoutedHttpBatch(URL[] urls, TransformerFactoryWrapper transformerFactory) {
    super(urls,transformerFactory);
  }

  @Override
  public synchronized void process(Job j) throws AspireException {
    HttpBatch batchToUse = null;
    String docID = null;
    URL nextUrl = null;
    AspireObject doc = j.get();

    if(deterministic) {
      if (StringUtilities.isEmpty(documentIDPath)) {
        postHttpStage.warn("Document ID field must be specified for deterministic method");
      }

      docID = ComponentImpl.getStringFromAOPath(doc, documentIDPath, null);

      if (StringUtilities.isEmpty(docID))
      {
        postHttpStage.warn("Document ID empty while using deterministic method (%s)", docID);
      }

      //Get batch reference to post.
      batchToUse = getNextBatchReference(docID);
      postHttpStage.debug("Deterministic url: %s", batchToUse.getPostUrl().toString());
    }
    else
    {
      //Straight round robin.
      batchToUse = getNextBatchReference(nextUrl);        
      postHttpStage.debug("Round-robin url: %s", batchToUse.getPostUrl().toString());
    }

    batchToUse.process(j);
  }
  
  /**
   * Gets the next URL to post to. Initially returns the head from the list, and advances the counter so
   * the next URL is returned next time (round robin).
   * @return the URL to post to
   */
  private synchronized HttpBatch getNextBatchReference(URL currURL) {
    //If there's only one, return it.
    if (serversUrl.length == 1)
      return batchOutputs[0];

    //Get the next URL and advance the counter.
    HttpBatch nextBatch = batchOutputs[nextServerUrlIndex];
    URL nextUrl = serversUrl[nextServerUrlIndex++];

    // Loop the counter
    nextServerUrlIndex = nextServerUrlIndex % serversUrl.length;

    //Ensure this URL is not the same as the last.
    if (currURL != null && currURL.equals(nextUrl))
    {
      //Get the next URL and advance the counter.
      nextBatch = batchOutputs[nextServerUrlIndex++];

      //Loop the counter.
      nextServerUrlIndex = nextServerUrlIndex % serversUrl.length;
    }

    //Return.
    return nextBatch;
  }

  
  
  /**
   * Gets the URL to post to. Uses a hash of the document ID to ensure that the same document is
   * always sent to the same server (deterministic).
   * @return the URL to post to
   */
  private HttpBatch getNextBatchReference(String docID) {
    
    //If there's only one of the docID is empty, return the first/only one.
    if (serversUrl.length == 1 || StringUtilities.isEmpty(docID))
      return batchOutputs[0];
    
    //Return the URL to use.
    return batchOutputs[Math.abs(docID.hashCode()) % serversUrl.length];
  }

  
  //Getters and setters
  
  public boolean isDeterministic() {
    return deterministic;
  }
  
  
  public void setDeterministic(boolean deterministic) {
    this.deterministic = deterministic;
  }
  
  
  public String getDocumentIDPath() {
    return documentIDPath;
  }
  
  
  public void setDocumentIDPath(String documentIDPath) {
    this.documentIDPath = documentIDPath;
  }
}
