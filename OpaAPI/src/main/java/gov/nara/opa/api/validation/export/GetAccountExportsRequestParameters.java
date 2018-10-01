package gov.nara.opa.api.validation.export;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;

import java.util.LinkedHashMap;

public class GetAccountExportsRequestParameters extends
    AbstractRequestParameters {

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    return requestParams;
  }
}
