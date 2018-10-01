package gov.nara.opa.architecture.web.controller.aspirehelper;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Super Class for all helpers used for creating API responses
 * 
 * @author aolaru
 * @date Jun 9, 2014
 * 
 */
public class AspireEntityObjectContentHolder extends
    AbstractWebEntityValueObject {

  protected LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

  public AspireEntityObjectContentHolder(
      LinkedHashMap<String, Object> aspireContent) {
    this.aspireContent = aspireContent;
  }

  protected AspireEntityObjectContentHolder() {
    super();
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return aspireContent;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

}
