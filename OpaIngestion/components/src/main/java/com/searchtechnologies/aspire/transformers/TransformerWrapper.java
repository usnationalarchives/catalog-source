/**
 * Copyright Search Technologies 2013
 */
package com.searchtechnologies.aspire.transformers;

import java.io.Writer;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

public interface TransformerWrapper {
  public void returnToPool() throws AspireException;
  public String getStringType();
  public void transformToWriter(AspireObject ao, Writer w) throws AspireException;
  public void setParam(String paramName, Object paramValue);
}
