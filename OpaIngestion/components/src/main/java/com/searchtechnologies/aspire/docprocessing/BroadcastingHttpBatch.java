/**
* Copyright Search Technologies 2011
*/
package com.searchtechnologies.aspire.docprocessing;

import java.net.URL;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.transformers.TransformerFactoryWrapper;

public class BroadcastingHttpBatch extends MultiServerHttpBatch {

  public BroadcastingHttpBatch(URL[] urls, TransformerFactoryWrapper transformerFactory) {
    super(urls,transformerFactory);
  }


  /**
   * Open connection to each server. Server connections are wrapped with an HttpBatch instance, which allows to handle batching for each connection individually.
   */
  @Override
  public void open() throws AspireException {
    super.open();
  }


  /**
   * Send the same data to each of the configured server URLs.
   */
  @Override
  public synchronized void process(Job j) throws AspireException {
    for (HttpBatch httpBatch : batchOutputs) {
      httpBatch.process(j);
    }
  }
}
