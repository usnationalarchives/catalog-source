package gov.nara.opa.api.services.search;

import java.net.MalformedURLException;
import java.util.TreeMap;

public interface OldApiSearchService {

  /**
   * Public API method to retrieve the search engine results
   * 
   * @param opaPath
   *          path variable for search engine
   * @param query
   *          search query string
   * @return Search Results Map
   * @throws MalformedURLException
   */
  public TreeMap<String, Object> getSearchResults(String opaPath, String query)
      throws MalformedURLException;
}
