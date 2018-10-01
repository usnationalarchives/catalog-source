package gov.nara.opa.api.services.search;

import gov.nara.opa.api.validation.search.StatisticsRequestParameters;

import java.util.LinkedHashMap;

public interface StatisticsService {
  public LinkedHashMap<String, Object> getStatistics(
      StatisticsRequestParameters requestParameters, String opaPath);
}
