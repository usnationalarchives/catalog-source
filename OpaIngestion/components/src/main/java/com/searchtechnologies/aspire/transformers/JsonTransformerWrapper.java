/**
 * Copyright Search Technologies 2013
 */
package com.searchtechnologies.aspire.transformers;

import java.io.IOException;
import java.io.Writer;

import com.searchtechnologies.aspire.groovy.JsonTransformer;
import com.searchtechnologies.aspire.groovy.JsonTransformerFactory;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

public class JsonTransformerWrapper implements TransformerWrapper {
  TransformerFactoryWrapper factory;
  JsonTransformer transformer;
  boolean onBatch = false;
  JsonTransformerWrapper(TransformerFactoryWrapper factory, boolean onBatch) throws AspireException {
    this.factory = factory;
    
    transformer = JsonTransformerFactory.newTransformer(factory.getPostJsonTransform());
    this.onBatch = onBatch;
  }

  @Override
  public String getStringType() {
    return factory.getTransformerType().name();
  }

  @Override
  public void returnToPool() {
    transformer.returnToPool();
  }

  @Override
  public void transformToWriter(AspireObject ao, Writer w) throws AspireException {
    transformer.transformToWriter(ao, w);
    try {
      w.write("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setParam(String paramName, Object paramValue) {
    transformer.setParam(paramName, paramValue);
  }
}
