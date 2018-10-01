package gov.nara.opa.api.services.print;

import gov.nara.opa.common.validation.print.PrintResultsRequestParameters;

import java.net.MalformedURLException;
import java.util.LinkedHashMap;

public interface PrintResultsService {

  /**
   * Method to retrieve the search engine print results
   * 
   * @param opaPath
   *          path variable for search engine
   * @param query
   *          search query string
   * @return LinkedHashMap of Print Results
   * @throws MalformedURLException
   */
  public LinkedHashMap<String, Object> getPrintResults(
      PrintResultsRequestParameters requestParameters, String opaPath,
      String query);
}
