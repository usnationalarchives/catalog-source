package gov.nara.opa.api.services.impl.search;

import gov.nara.opa.api.search.FullResults;
import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.api.services.search.OldApiSearchService;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;

import java.net.MalformedURLException;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OldApiSearchServiceImpl implements OldApiSearchService {

  private static OpaLogger logger = OpaLogger
      .getLogger(OldApiSearchServiceImpl.class);

  @Autowired
  private GetLoadBalancedSolrServer getLoadBalancedSolrServer;

  LoadBalancedHttpSolrServer solrServer;

  @Override
  public TreeMap<String, Object> getSearchResults(String opaPath, String query)
      throws MalformedURLException {
    TreeMap<String, Object> searchResults = new TreeMap<String, Object>();
    TreeMap<Integer, FullResults> documentFullResults = new TreeMap<Integer, FullResults>();
    Float score = 0.0f;
    String type = "";
    String naId = "";
    String opaId = "";
    String url = "";
    String description = "";
    String authority = "";
    String objects = "";
    int totalSearchResults = 0;
    double queryTime = 0.0;

    query = query + "&resultFields=type,naId,opaId,description,"
        + "authority,objects";

    try {
      /**********************************************************************************/
      // START: Using Solr Software Load Balancing
      solrServer = getLoadBalancedSolrServer.getServer();
      // END: Using Solr Software Load Balancing
      /**********************************************************************************/

      // Build the Solr parameters
      SolrUtils sUtils = new SolrUtils();
      SolrParams solrParams = sUtils.makeParams(opaPath, query, -1);

      // Execute the search
      QueryResponse qryResponse = solrServer.query(solrParams);

      // Extract the search results
      SolrDocumentList resultsList = qryResponse.getResults();

      // Retrieve the total search results amount
      totalSearchResults = (int) qryResponse.getResults().getNumFound();

      // Retrieve the query response time
      queryTime = qryResponse.getQTime();

      searchResults.put("@total", totalSearchResults);
      searchResults.put("queryTime", queryTime);

      // Process search results
      for (int docNumber = 0; docNumber < resultsList.size(); docNumber++) {
        SolrDocument doc = resultsList.get(docNumber);

        // Retrieve the score
        if (doc.getFieldValue("score") != null)
          score = (Float) doc.getFieldValue("score");

        // Retrieve the type
        if (doc.getFieldValue("type") != null)
          type = (String) doc.getFieldValue("type");

        // Retrieve the naId
        if (doc.getFieldValue("naId") != null)
          naId = StringUtils.removeMarkUps((String) doc.getFieldValue("naId"));

        // Retrieve the opaId
        if (doc.getFieldValue("opaId") != null)
          opaId = (String) doc.getFieldValue("opaId");

        // Retrieve the url
        if (doc.getFieldValue("url") != null)
          url = (String) doc.getFieldValue("url");

        // Retrieve the description
        if (doc.getFieldValue("description") != null)
          description = (String) doc.getFieldValue("description");

        // Retrieve the authority
        if (doc.getFieldValue("authority") != null)
          authority = (String) doc.getFieldValue("authority");

        // Retrieve the objects
        if (doc.getFieldValue("objects") != null)
          objects = (String) doc.getFieldValue("objects");

        // Retrieve the document breif results
        FullResults fullResultsObj = new FullResults();

        // Build the full results object
        fullResultsObj.setDocNumber(docNumber);
        fullResultsObj.setScore(score);
        fullResultsObj.setType(type);
        fullResultsObj.setNaId(naId);
        fullResultsObj.setOpaId(opaId);
        fullResultsObj.setUrl(url);
        fullResultsObj.setDescription(description);
        fullResultsObj.setAuthority(authority);
        fullResultsObj.setObjects(objects);

        // Build the brief results map to return
        documentFullResults.put(docNumber, fullResultsObj);
      }
      searchResults.put("documentFullResults", documentFullResults);

    } catch (SolrServerException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    return searchResults;
  }

}
