package gov.nara.opa.api.services.search;

import java.net.MalformedURLException;
import java.util.TreeMap;

public interface OpaSearchService {

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
  public TreeMap<String, Object> getBriefResults(String opaPath, String query,
      boolean webGrouping) throws MalformedURLException;

  int getListLimitForUser();
}
