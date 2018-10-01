package gov.nara.opa.api.services.search;

import gov.nara.opa.api.validation.search.BriefResultsRequestParameters;

import java.net.MalformedURLException;
import java.util.LinkedHashMap;

public interface BriefResultsSearchService {

  /**
   * Method to retrieve the search engine brief results
   * 
   * @param opaPath
   *          path variable for search engine
   * @param query
   *          search query string
   * @return Brief Results Map totalResults ==> total search results returned
   *         queryTime ==> query response time briefResults ==> map of breif
   *         results (docNumber ==> BreifResults Object)
   * @throws MalformedURLException
   */
  public LinkedHashMap<String, Object> getBriefResults(
      BriefResultsRequestParameters requestParameters, String opaPath,
      String query, String accountType);

  //int getSearchLimitForUser(String accountType);
}
