package gov.nara.opa.api.validation.search;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;

import java.util.LinkedHashMap;

public class StatisticsRequestParameters extends AbstractRequestParameters {

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();

    return requestParams;
  }
}
