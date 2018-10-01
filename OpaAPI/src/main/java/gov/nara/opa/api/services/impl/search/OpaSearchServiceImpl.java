package gov.nara.opa.api.services.impl.search;

import gov.nara.opa.api.search.BriefResults;
import gov.nara.opa.api.search.WebResults;
import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.api.services.search.OpaSearchService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpaSearchServiceImpl implements OpaSearchService {

  private static OpaLogger logger = OpaLogger
      .getLogger(OpaSearchServiceImpl.class);

  @Autowired
  private GetLoadBalancedSolrServer getLoadBalancedSolrServer;

  LoadBalancedHttpSolrServer solrServer;

  @Autowired
  private ConfigurationService configurationService;

  @SuppressWarnings("unchecked")
  @Override
  public TreeMap<String, Object> getBriefResults(String opaPath, String query,
      boolean webGrouping) throws MalformedURLException {
    TreeMap<String, Object> briefResults = new TreeMap<String, Object>();
    TreeMap<Integer, BriefResults> documentBriefResults = new TreeMap<Integer, BriefResults>();
    TreeMap<Integer, WebResults> documentWebResults = new TreeMap<Integer, WebResults>();
    int totalSearchResults = 0;
    double queryTime = 0.0;

    String webQuery = query + "&resultFields=naId,opaId,title,"
        + "webArea,webAreaUrl,url,iconType,teaser";

    query = query + "&resultFields=naId,opaId,url,iconType,"
        + "thumbnailFile,hasOnline,tabType,teaser";

    if (query.indexOf("tabType=all") > 0) {
      query = query + "&filter=(tabType:all%20and%20not(source:web))";
    }

    try {
      /**********************************************************************************/
      // START: Using Solr Software Load Balancing
      solrServer = getLoadBalancedSolrServer.getServer();
      // END: Using Solr Software Load Balancing
      /**********************************************************************************/

      logger.info(" Query: " + query);

      // Build the Solr parameters
      SolrUtils sUtils = new SolrUtils();
      SolrParams solrParams = sUtils.makeParams(opaPath, query, -1);

      // Execute the search
      QueryResponse qryResponse = solrServer.query(solrParams, METHOD.POST);

      // Extract the search results
      SolrDocumentList resultsList = qryResponse.getResults();

      // Retrieve the total search results amount
      totalSearchResults = (int) qryResponse.getResults().getNumFound();

      // Retrieve the query response time
      queryTime = qryResponse.getQTime();

      logger.info(" QueryTime: " + queryTime);

      briefResults.put("@total", totalSearchResults);
      briefResults.put("queryTime", queryTime);

      SimpleOrderedMap<Object> solrBriefResults = new SimpleOrderedMap<Object>();
      solrBriefResults = (SimpleOrderedMap<Object>) qryResponse.getResponse()
          .get("briefResults");

      // Process search results
      for (int docNumber = 0; docNumber < resultsList.size(); docNumber++) {
        SolrDocument doc = resultsList.get(docNumber);

        Float score = 0.0f;
        String naId = "";
        String opaId = "";
        String url = "";
        String iconType = "";
        String thumbnailFile = "";
        boolean hasOnline = false;
        List<String> tabType = new ArrayList<String>();
        String teaser = "";

        // Retrieve the score
        if (doc.getFieldValue("score") != null)
          score = (Float) doc.getFieldValue("score");

        // Retrieve the naId
        if (doc.getFieldValue("naId") != null)
          naId = StringUtils.removeMarkUps((String) doc.getFieldValue("naId"));

        // Retrieve the opaId
        if (doc.getFieldValue("opaId") != null)
          opaId = (String) doc.getFieldValue("opaId");

        // Retrieve the url
        if (doc.getFieldValue("url") != null)
          url = (String) doc.getFieldValue("url");

        // Retrieve the iconType
        if (doc.getFieldValue("iconType") != null)
          iconType = (String) doc.getFieldValue("iconType");

        // Retrieve the thumbnailFile
        if (doc.getFieldValue("thumbnailFile") != null)
          thumbnailFile = (String) doc.getFieldValue("thumbnailFile");

        // Retrieve the hasOnline
        if (doc.getFieldValue("hasOnline") != null
            && (doc.getFieldValue("hasOnline").equals("true") || doc
                .getFieldValue("hasOnline").equals("TRUE")))
          hasOnline = true;
        else
          hasOnline = false;

        // Retrieve the tabType
        if (doc.getFieldValue("tabType") != null)
          tabType = (List<String>) doc.getFieldValue("tabType");

        // Retrieve the teaser
        if (doc.getFieldValue("teaser") != null)
          teaser = (String) doc.getFieldValue("teaser");

        // Retrieve the document breif results
        BriefResults briefResultsObj = new BriefResults();

        HashMap<String, ArrayList<Map<String, Object>>> solrDocumentBriefResults = new HashMap<String, ArrayList<Map<String, Object>>>();
        solrDocumentBriefResults = (HashMap<String, ArrayList<Map<String, Object>>>) solrBriefResults
            .get(opaId);

        // Build the brief results object
        briefResultsObj.setDocNumber(docNumber);
        briefResultsObj.setScore(score);
        briefResultsObj.setNaId(naId);
        briefResultsObj.setOpaId(opaId);
        briefResultsObj.setUrl(url);
        briefResultsObj.setIconType(iconType);
        briefResultsObj.setThumbnailFile(thumbnailFile);
        briefResultsObj.setHasOnline(hasOnline);
        briefResultsObj.setTabType(tabType);
        briefResultsObj.setTeaser(teaser);
        briefResultsObj.setDocumentBriefResults(solrDocumentBriefResults);

        // Build the brief results map to return
        documentBriefResults.put(docNumber, briefResultsObj);
      }

      // Extract the facet results
      ArrayList<LinkedHashMap<String, Object>> facetResultsList = new ArrayList<LinkedHashMap<String, Object>>();
      if (qryResponse.getFacetFields() != null) {
        List<FacetField> fflist = qryResponse.getFacetFields();
        for (FacetField ff : fflist) {
          LinkedHashMap<String, Object> facetResults = new LinkedHashMap<String, Object>();
          String facetLabel = ff.getName();
          List<Count> counts = ff.getValues();
          ArrayList<LinkedHashMap<String, Object>> facetValuesList = new ArrayList<LinkedHashMap<String, Object>>();
          for (Count c : counts) {
            LinkedHashMap<String, Object> facetValuesMap = new LinkedHashMap<String, Object>();
            String facetValue = c.getName();
            long facetCount = c.getCount();
            facetValuesMap.put("@name", facetValue);
            facetValuesMap.put("@count", facetCount);
            facetValuesList.add(facetValuesMap);
          }
          facetResults.put("@name", facetLabel);
          facetResults.put("v", facetValuesList);
          facetResultsList.add(facetResults);
        }
      }

      briefResults.put("documentBriefResults", documentBriefResults);
      briefResults.put("facetResults", facetResultsList);

      // If the user requests web group results
      if (webGrouping) {

        // Perform a second search to retrieve the top 3 web results
        webQuery = webQuery.replaceAll("tabType=all", "tabType=webInsert");

        // Build the Solr parameters
        SolrParams webSolrParams = sUtils.makeParams(opaPath, webQuery, -1);

        // Execute the web search
        QueryResponse webQryResponse = solrServer.query(webSolrParams);

        // Extract the web search results
        SolrDocumentList webResultsList = webQryResponse.getResults();

        // Process web search results
        for (int docNumber = 0; docNumber < webResultsList.size(); docNumber++) {
          SolrDocument doc = webResultsList.get(docNumber);

          Float score = 0.0f;
          String naId = "";
          String opaId = "";
          String title = "";
          String webArea = "";
          String webAreaUrl = "";
          String url = "";
          String iconType = "";
          String teaser = "";

          // Retrieve the score
          if (doc.getFieldValue("score") != null)
            score = (Float) doc.getFieldValue("score");

          // Retrieve the naId
          if (doc.getFieldValue("naId") != null)
            naId = StringUtils
                .removeMarkUps((String) doc.getFieldValue("naId"));

          // Retrieve the opaId
          if (doc.getFieldValue("opaId") != null)
            opaId = (String) doc.getFieldValue("opaId");

          // Retrieve the title
          if (doc.getFieldValue("title") != null)
            title = (String) doc.getFieldValue("title");

          // Retrieve the webArea
          if (doc.getFieldValue("webArea") != null)
            webArea = (String) doc.getFieldValue("webArea");

          // Retrieve the webAreaUrl
          if (doc.getFieldValue("webAreaUrl") != null)
            webAreaUrl = (String) doc.getFieldValue("webAreaUrl");

          // Retrieve the url
          if (doc.getFieldValue("url") != null)
            url = (String) doc.getFieldValue("url");

          // Retrieve the iconType
          if (doc.getFieldValue("iconType") != null)
            iconType = (String) doc.getFieldValue("iconType");

          // Retrieve the teaser
          if (doc.getFieldValue("teaser") != null)
            teaser = (String) doc.getFieldValue("teaser");

          // Build the web results object
          WebResults webResultsObj = new WebResults();
          webResultsObj.setDocNumber(docNumber);
          webResultsObj.setScore(score);
          webResultsObj.setNaId(naId);
          webResultsObj.setOpaId(opaId);
          webResultsObj.setTitle(title);
          webResultsObj.setWebArea(webArea);
          webResultsObj.setWebAreaUrl(webAreaUrl);
          webResultsObj.setUrl(url);
          webResultsObj.setIconType(iconType);
          webResultsObj.setTeaser(teaser);

          // Build the brief results map to return
          documentWebResults.put(docNumber, webResultsObj);
        }
        briefResults.put("webResults", documentWebResults);
      }

    } catch (SolrServerException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    return briefResults;
  }

  @Override
  public int getListLimitForUser() {

    UserAccount sessionUser = SessionUtils.getSessionUser();

    if (sessionUser != null) {
      switch (sessionUser.getAccountType().toLowerCase()) {
        case "standard":
          return configurationService.getConfig()
              .getMaxResultsPerListStandard();
        case "power":
          return configurationService.getConfig().getMaxResultsPerListPower();
      }

    } else {
      return configurationService.getConfig().getMaxResultsPerListPublic();
    }

    return 0;
  }

}
