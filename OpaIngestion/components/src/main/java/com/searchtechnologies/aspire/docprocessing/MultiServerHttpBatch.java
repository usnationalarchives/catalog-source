/**
* Copyright Search Technologies 2011
*/
package com.searchtechnologies.aspire.docprocessing;

import java.net.URL;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.transformers.TransformerFactoryWrapper;

public abstract class MultiServerHttpBatch extends HttpBatch {

  /**
   * Array of batch objects used to stream content to multiple servers.
   */
  HttpBatch batchOutputs[] = null;

  /**
   * Array of server URLs to post.
   */
  URL serversUrl[] = null;

  /**
   * Transformer Factory to be set to each HttpBatch
   */
  TransformerFactoryWrapper transformerFactory;

  public MultiServerHttpBatch(URL[] urls, TransformerFactoryWrapper transformerFactory) {
    this.serversUrl = urls;
    this.transformerFactory = transformerFactory;
  }


  /**
   * When multiple servers URL are configured, we don't open the connections until required.
   */
  @Override
  public void open() throws AspireException {
    batchOutputs = new HttpBatch[serversUrl.length];

    for(int index = 0; index < serversUrl.length; index++) {
      batchOutputs[index] = new HttpBatch();

      batchOutputs[index].setPostUrl(serversUrl[index]);
      
      batchOutputs[index].setTransformerFactory(transformerFactory);
      
      //Set common properties to batch reference.
      //batchOutputs[index].setPostXslUrl(postXslUrl);
      batchOutputs[index].setMaxTries(maxTries);
      batchOutputs[index].setRetryWait(retryWait);
      batchOutputs[index].setMultipartForm(multipartForm);
      batchOutputs[index].setMultipartParams(multipartParams);
      batchOutputs[index].setMultipartContentParam(multipartContentParam);
      batchOutputs[index].setOkayResponse(okayResponse);
      batchOutputs[index].setPostHttpStage(postHttpStage);

      //Set batching parameters to batch reference.
      batchOutputs[index].setPostHeader(postHeader);
      batchOutputs[index].setPostFooter(postFooter);
    }
  }

  
  @Override
  public synchronized void closeConnection() throws AspireException {
    boolean errorOccurred = false;
    Throwable exception = null;
    
    for (HttpBatch batch : batchOutputs) {
      if(batch.isOpen()) {
        try {
          batch.closeConnection();
        }
        catch(Throwable t) {
          if(!errorOccurred) {
            // Only save the first one
            errorOccurred = true;
            exception = t;
            this.serverResponse = batch.getServerResponse();
          }
          else {
            // Just log all of the others.
            postHttpStage.error(t, "Encountered error closing multi-server connections.");
          }
        }
      }
    }
    
    if(exception != null) {
      throw new AspireException("MultiServerHttpBatch.error-closing-connections", exception, "Encountered error closing up HTTPMultiServer connections. Message = " + exception.getMessage());
    }
  }


  @Override
  public abstract void process(Job j) throws AspireException;


  @Override
  public String getServerResponse() {
    for (HttpBatch batch : batchOutputs) {
      if(batch != null && batch.getServerResponse() != null) {
        return batch.getServerResponse();
      }
    }
    
    return null;
  }

  //Getters and setters
  
  public URL[] getServerUrls() {
    return serversUrl;
  }
  
  
  public void setServersUrl(URL[] serversUrl) {
    this.serversUrl = serversUrl;
  }
  
}
