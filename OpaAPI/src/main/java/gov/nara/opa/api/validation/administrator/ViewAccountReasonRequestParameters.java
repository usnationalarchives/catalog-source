package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObjectConstants;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;

import java.util.LinkedHashMap;

public class ViewAccountReasonRequestParameters extends
    AbstractRequestParameters implements AccountReasonValueObjectConstants {

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    return requestParams;
  }

}
