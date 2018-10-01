package gov.nara.opa.architecture.web.valueobject;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.util.LinkedHashMap;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

/**
 * Root super class for all objects. See AspireObjectCreator for methods
 * documentation
 * 
 * @author aolaru
 * @date Jun 9, 2014
 * 
 */
public abstract class AbstractWebValueObject implements AspireObjectCreator {
  private AspireObject aspireObject;

  @Override
  public abstract LinkedHashMap<String, Object> getAspireObjectContent(
      String action);

  @Override
  public AspireObject createAspireObject(String objectName, String action,
      boolean fetchFromCache) {
    if (fetchFromCache && aspireObject != null) {
      return aspireObject;
    }
    try {
      aspireObject = ValueObjectUtils.createAspireObjectFromContent(objectName,
          this, action);
    } catch (AspireException ex) {
      throw new OpaRuntimeException(ex);
    }
    return aspireObject;
  }

  @Override
  public String toString() {
    return "Instance of - " + this.getClass().getName()
        + " - parameters values:\n" + getAspireObjectContent(null).toString();
  }

}
