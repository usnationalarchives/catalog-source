package gov.nara.opa.architecture.web.controller.aspirehelper;

import gov.nara.opa.architecture.web.valueobject.AbstractWebValueObject;

import java.util.LinkedHashMap;

/**
 * Super Class for all helpers used for creating API responses
 * 
 * @author aolaru
 * @date Jun 9, 2014
 * 
 */
public class AspireObjectContentHolder extends AbstractWebValueObject {

  public static final String OPA_RESPONSE = "opaResponse";

  protected LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

  public AspireObjectContentHolder(LinkedHashMap<String, Object> aspireContent) {
    this.aspireContent = aspireContent;
  }

  protected AspireObjectContentHolder() {
    super();
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return aspireContent;
  }

}
