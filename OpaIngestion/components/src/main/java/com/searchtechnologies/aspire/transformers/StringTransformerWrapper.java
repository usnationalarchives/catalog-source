/**
 * Copyright Search Technologies 2013
 */
package com.searchtechnologies.aspire.transformers;

import java.io.IOException;
import java.io.Writer;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

public class StringTransformerWrapper implements TransformerWrapper {
  TransformerFactoryWrapper factory;
  
  StringTransformerWrapper(TransformerFactoryWrapper factory, boolean onBatch) {
    this.factory = factory;
  }

  @Override
  public String getStringType() {
    return factory.getTransformerType().name();
  }

  @Override
  public void returnToPool() {
    // Do nothing. Strings are not pooled.
  }

  @Override
  public void transformToWriter(AspireObject ao, Writer w) throws AspireException {
    try {
      w.write(factory.getPostString());
    } catch (IOException e) {
      throw new AspireException("com.searchtechnologies.aspire.transformers.StringTransformerWrapper.transformToWriter",e,"IO Error while trying to write string to writer");
    }
  }

  @Override
  public void setParam(String paramName, Object paramValue) {
    // Do nothing. Strings do not have parameters
  }
}
