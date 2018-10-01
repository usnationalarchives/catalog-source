/**
 * Copyright Search Technologies 2013
 */
package com.searchtechnologies.aspire.transformers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.ATransformer;
import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.framework.utilities.StringUtilities;
import com.searchtechnologies.aspire.framework.utilities.Utilities;
import com.searchtechnologies.aspire.groovy.JsonTransformer;
import com.searchtechnologies.aspire.groovy.JsonTransformerFactory;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.LoggerComponent;

public class TransformerFactoryWrapper implements Cloneable {
  LoggerComponent parentComponent;
  /**
   * Path to the XSL file used for transforming Aspire Documents.
   */
  private String postXsl = null;

  /**
   * URL to the XSL file used for transforming Aspire Documents.
   */
  private URL postXslUrl = null;

  /**
   * The JSON transformmer (a Groovy builder) used when posting JSON.
   */
  private String postJsonTransform = null;
  
  /**
   * Static string to be posted. This can be a command (in Solr a <commit> command for example) or an static string. If this field is configured, then it will be used instead of XSL transformations to the Aspire Document.
   */
  protected String postString = null;

  /** Specifies the type of transformer that this processor uses.
   */
  enum TransformTypeEnum {UNKNOWN, JAVA, SAXON, JSON, STRING};
  TransformTypeEnum transformerType = TransformTypeEnum.UNKNOWN;
  
  public TransformerFactoryWrapper(LoggerComponent parentComponent) {
    this.parentComponent = parentComponent;
  }
  
  @SuppressWarnings("unused")
  public void initialize(Element config) throws AspireException {
    postString = ComponentImpl.getStringFromConfig(config, "postString", postString);
    postXsl = ComponentImpl.getStringFromConfig(config, "postXsl", null);
    postJsonTransform = ComponentImpl.getStringFromConfig(config, "postJsonTransform", null);
    
    if (postJsonTransform!=null)
      postJsonTransform = getFilePathFromAspireHome(postJsonTransform);
    
    
    boolean useSaxon = ComponentImpl.getBooleanFromConfig(config, "saxonProcessor", false);
    if (useSaxon) transformerType = TransformTypeEnum.SAXON;

    // Compute transformer type
    if(postXsl != null) { 
      if (useSaxon) transformerType = TransformTypeEnum.SAXON;
      else transformerType = TransformTypeEnum.JAVA;
      postXslUrl = getUrlFromAspireHome(postXsl);
    }
    
    if(postJsonTransform != null) {
      if(transformerType != TransformTypeEnum.UNKNOWN)
        parentComponent.warn("Post-HTTP stage specified both an XSL and JSON transformer. Only one is allowed. " +
        		"Ignoring the <postJsonTransform> configuration parameter.");
      else
        transformerType = TransformTypeEnum.JSON;
    }
    
    if(postString != null) {
      if(transformerType != TransformTypeEnum.UNKNOWN)
        parentComponent.warn("Post-HTTP stage specified both a %s and a POST-STRING transformer. " +
            "Only one is allowed. Ignoring the <postString> configuration parameter.", getStringType());
      else
        transformerType = TransformTypeEnum.STRING;
    }
    
    // ** Preload the transformer:  TEST TO SEE IF THE SCRIPTS COMPILE
    
    parentComponent.debug("Using %s to post Aspire Document.", getStringType());

    TransformerWrapper transformerWrapper = null;
    
    switch(transformerType) {
      // *** PRELOAD THE XSLT TRANSFORMER
      case JAVA:
      case SAXON:
        transformerWrapper = new XmlTransformerWrapper(this,false); 
        break;
        
      case JSON:
        transformerWrapper = new JsonTransformerWrapper(this,false);
        break;
        
      case STRING: 
        transformerWrapper = new StringTransformerWrapper(this,false);
        break;
      
      default:
        throw new AspireException(parentComponent,"aspire.PostXmlStage.invalid-post", 
        "Either <postString> (the command for the post) or <postXsl> (the XSL for the post) or <postJsonTransform> (the Json Transformer / Groovy Json builder) must be specified in the <config> element");
    }
    
    //transformerWrapper.returnToPool();  // Now put the one pre-loaded transformer back on the pool to be re-used later
  }
  
  public TransformerWrapper newTransformer(boolean forBatches) throws AspireException {
    switch(transformerType) {
      case JAVA: 
      case SAXON:  return new XmlTransformerWrapper(this,forBatches);
      case JSON:   return new JsonTransformerWrapper(this,forBatches);
      case STRING: return new StringTransformerWrapper(this,forBatches);
      default:
        throw new AspireException(parentComponent,"TransformerFactoryWrapper.newTransformerUnknownType", 
        "The transformer type (%s) is unknown.", transformerType);
    }
  }
  
  public void clearPool() throws AspireException {
    switch(transformerType) {
      case JAVA: 
      case SAXON: {
        ATransformer.clearPool(postXslUrl);
        break;
      }

      case JSON: {
        JsonTransformerFactory.clearPool(postJsonTransform);
        break;
      }
      
      case STRING: 
        break;
        
      default:
        throw new AspireException(parentComponent,"TransformerFactoryWrapper.newTransformerUnknownType", 
        "The transformer type (%s) is unknown.", transformerType);
    }
  }
  
  
  public void reload() throws AspireException {
    switch(transformerType) {
      case JAVA: 
      case SAXON: {
        ATransformer.clearPool(postXslUrl);
        ATransformer transformer;
        try {
          transformer = ATransformer.getInstance(postXslUrl, transformerType.name());
          transformer.returnToPool();
        }
        catch(AspireException e) {
          throw e;
        }
        break;
      }

      case JSON: {
        JsonTransformerFactory.clearPool(postJsonTransform);
        JsonTransformer transformer = JsonTransformerFactory.newTransformer(postJsonTransform);
        transformer.returnToPool();
        break;
      }
      
      case STRING: 
        // Nothing to reload with strings
        break;
      default:
        throw new AspireException(parentComponent,"TransformerFactoryWrapper.newTransformerUnknownType", 
        "The transformer type (%s) is unknown.", transformerType);
    }

}

  /** Fetches a user-friendly name for the currently configured transformer for showing in error messages.
   * @return A user-friendly description of the currently configured transformer.
   */
  public String getStringType() {
    switch(transformerType) {
      case JAVA: return "XSLT 1.0 (standard Java)";
      case SAXON: return "XSLT 2.0 (Saxon)";
      case JSON: return "JSON";
      case STRING:  return "STRING";
      default:
        return "Unknown";
    }
  }
  
  /** Fetches the file name / string of the currently configured transformer for showing in error messages.
   * @return The currently configured file-name or string for the transformer.
   */
  public String getTransformerFileName() {
    switch(transformerType) {
      case JAVA: 
      case SAXON: return postXsl;
      case JSON: return postJsonTransform;
      case STRING: return "String[" + postString + "]";
      default:
        return "Unknown Transform File Name";
    }
  }
  
  public void addStatus(AspireObject status) {
    status.setAttribute("postString", StringUtilities.emptyNull(getPostString()));
    status.setAttribute("postXslUrl", getPostXslUrl() != null ? getPostXslUrl().toString() : "");
    status.setAttribute("postXsl", StringUtilities.emptyNull(getPostXsl()));
  }

  public Component getParentComponent() {
    return parentComponent;
  }

  public String getPostXsl() {
    return postXsl;
  }

  public URL getPostXslUrl() {
    return postXslUrl;
  }

  public String getPostJsonTransform() {
    return postJsonTransform;
  }

  public String getPostString() {
    return postString;
  }

  protected TransformTypeEnum getTransformerType() {
    return transformerType;
  }
  
  @Override
  public TransformerFactoryWrapper clone() {
    TransformerFactoryWrapper clone = new TransformerFactoryWrapper(parentComponent);
    if (this.postXsl!=null){
      clone.postXsl = new String(this.postXsl);
      try {
        clone.postXslUrl = getUrlFromAspireHome(clone.postXsl);
      } catch (AspireException e) {
        e.printStackTrace();
        return null;
      }
    }
    if (this.postJsonTransform!=null)
      clone.postJsonTransform = new String(this.postJsonTransform);
    if (this.postString!=null)
      clone.postString = new String(this.postString);
    clone.transformerType = this.transformerType;
    return clone;
  }
  
  public void setPostXsl(String postXsl) throws AspireException{
    this.postXsl = postXsl;
    this.postXslUrl = getUrlFromAspireHome(postXsl);
  }

  public String getContentType() {
    switch(transformerType) {
      case JAVA: 
      case SAXON: return "text/xml";
      case JSON: return "application/json";
      case STRING: return "text/plain";
      default:
        return "text/plain";
    }
  }

  public URL getUrlFromAspireHome(String inputPath) throws AspireException {
    String homeDir = "unknown";
    try {
      if(new File(inputPath).isAbsolute())
        return (new File( inputPath)).toURI().toURL();
      else {
        homeDir = Utilities.getAspireHome();
        return (new File( homeDir + "/" + inputPath)).toURI().toURL();
      }
    }
    catch (MalformedURLException e) {
      throw new AspireException(parentComponent, "aspire.framework.ComponentImpl.malformed-aspire-home-url",
          e,
          "Unable to construct a URL for a relative path based on ASPIRE_HOME. Path which we tried to convert was \"%s\".",
          homeDir + "/" + inputPath);
    }
  }

  
  /** Convert a file path to one which is based on the component home. If the source file
   * path is already an absolute path, return the original path. If the path is
   * a relative path, then return a path which is based on component home.
   * @param path The path to convert.
   * @return A new path (possibly relative) based on the component home location, if the
   * the original path was relative.
   */
  public String getFilePathFromAspireHome(String path) {
    File f = new File(path);
    if(f.isAbsolute())
      return path;
    else
      return Utilities.getAspireHome() + ((path.startsWith("/"))?"":"/") + path;
  }
}
