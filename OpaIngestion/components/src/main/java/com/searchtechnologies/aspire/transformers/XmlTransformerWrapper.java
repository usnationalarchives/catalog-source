/**
 * Copyright Search Technologies 2013
 */
package com.searchtechnologies.aspire.transformers;

import java.io.Writer;

import javax.xml.transform.OutputKeys;

import com.searchtechnologies.aspire.framework.ATransformer;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

public class XmlTransformerWrapper implements TransformerWrapper {
  TransformerFactoryWrapper factory;
  ATransformer transformer;

  XmlTransformerWrapper(TransformerFactoryWrapper factory, boolean onBatch) throws AspireException {
    this.factory = factory;
    
    transformer = ATransformer.getInstance(factory.getPostXslUrl(), factory.getTransformerType().name());
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
  }

  @Override
  public String getStringType() {
    return factory.getTransformerType().name();
  }

  @Override
  public void returnToPool() throws AspireException {
    transformer.returnToPool();
  }

  @Override
  public void transformToWriter(AspireObject ao, Writer w) throws AspireException {
    transformer.transformToWriter(ao, w);
  }

  public void omitXmlDeclaration(boolean omitDeclaration) {
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitDeclaration ? "yes" : "no");
  }

  @Override
  public void setParam(String paramName, Object paramValue) {
    transformer.setParam(paramName, paramValue.toString());
  }

}
