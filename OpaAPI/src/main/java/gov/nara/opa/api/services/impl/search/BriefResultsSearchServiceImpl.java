package gov.nara.opa.api.services.impl.search;

import gov.nara.opa.api.services.search.BriefResultsSearchService;
import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.utils.NullableUtils;
import gov.nara.opa.api.utils.SearchUtils;
import gov.nara.opa.api.validation.search.BriefResultsRequestParameters;
import gov.nara.opa.api.valueobject.search.BriefResultsCollectionValueObject;
import gov.nara.opa.api.valueobject.search.BriefResultsIntermediateValueObject;
import gov.nara.opa.api.valueobject.search.BriefResultsValueObject;
import gov.nara.opa.api.valueobject.search.SearchWithinValueObject;
import gov.nara.opa.api.valueobject.search.WebResultsCollectionValueObject;
import gov.nara.opa.api.valueobject.search.WebResultsValueObject;
import gov.nara.opa.architecture.exception.OpaApiResponseRuntimeException;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer.RemoteSolrException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

/**
 * Improved exception handling. Upon an exception, an erro id is genrated and logged with
 * the exception. Then an OpaRuntimeException is generated and throw to be handled by
 * the controller.
 */
@Component
public class BriefResultsSearchServiceImpl implements BriefResultsSearchService {

  private static OpaLogger logger = OpaLogger
      .getLogger(BriefResultsSearchServiceImpl.class);

  private static final String RESULT_FIELDS = "&resultFields=naId,opaId,url,iconType,thumbnailFile,hasOnline,tabType,teaser,shortContent,isOnline,objects";
  private static final String SEARCH_WITHIN_QUERY = "?action=searchWithin&q=naId:%1$s"
      + "&apiType=iapi&resultFields=naId,opaId,title,"
      + "ancestorNaIds,parentNaId,parentLevel,parentTitle,"
      + "doTopLevelSearch,topParentLevel,topParentTitle,"
      + "hierachy,url,iconType,thumbnailFile,hasOnline,"
      + "tabType,teaser,shortContent,isOnline";
  private static final String HIGHLIGHT = "&hl=true&hl.fl=title,creators,location,teaser,naId";

  @Autowired
  private OpaStorageFactory opaStorageFactory;

  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private GetLoadBalancedSolrServer getLoadBalancedSolrServer;

  @Autowired
  private SolrUtils sUtils;


    @Value("${naraBaseUrl}")
    private String naraBaseUrl;

  LoadBalancedHttpSolrServer solrServer;

  private static HashMap<String, String> labelMap = new HashMap<String, String>();

  @Override
  public LinkedHashMap<String, Object> getBriefResults(
      BriefResultsRequestParameters requestParameters, String opaPath,
      String query, String accountType) {
    LinkedHashMap<String, Object> allBriefResultsValuesMap = new LinkedHashMap<String, Object>();
    int totalBriefResults = 0;

    int maxRowsForUser = configurationService
        .getSearchLimitForUser(accountType);

    long stTm = System.currentTimeMillis();
    long endTm = System.currentTimeMillis();
    boolean excecuteSearchWithinQuery = false;

    int search_timeout = configurationService.getConfig().getSearchRunTime();

    initializeLabelMap();

    /*************** BUILD QUERY ********************/

    String searchWithinQuery = buildSearchWithinQuery(requestParameters, query);

    query = buildQuery(requestParameters, searchWithinQuery);
    
    /*********** END BUILD QUERY ********************/

    /*************** INIT SEARCH WITHIN QUERY ********************/
    String queryAction = getQueryAction(requestParameters);
    excecuteSearchWithinQuery = queryAction.equalsIgnoreCase("searchWithin");

    String ancestorNaId = getAncestorNaId(requestParameters, searchWithinQuery,
        excecuteSearchWithinQuery);
    /*************** END INIT SEARCH WITHIN QUERY ********************/

    try {
      /******************** GET SEARCH SERVER ***************************************/
      solrServer = getLoadBalancedSolrServer.getServer();
      /******************** END GET SEARCH SERVER ***************************************/

      /******************** PERFORM SEARCH **********************************************/
      // Perform search request
      stTm = System.currentTimeMillis();

      QueryResponse qryResponse = performSearch(query, opaPath, search_timeout,
          sUtils);

      endTm = System.currentTimeMillis();
      logger.info(" queryTime: " + ((endTm - stTm) / 1000f));

      /******************** END PERFORM SEARCH ******************************************/

      /******************** PROCESS BASIC SEARCH RESULTS ********************************/
      // Extract the search results
      SolrDocumentList resultsList = qryResponse.getResults();

      // Retrieve the total search results amount
      totalBriefResults = (int) qryResponse.getResults().getNumFound();

      BriefResultsIntermediateValueObject intermediateResults = new BriefResultsIntermediateValueObject(
          qryResponse);

      // Process search results
      List<BriefResultsValueObject> briefResults = new ArrayList<BriefResultsValueObject>();
      processSearchResults(resultsList, intermediateResults, briefResults);

      logger.info(" Total Brief Results :   " + totalBriefResults);

      // Set the brief results values
      BriefResultsCollectionValueObject brvo = new BriefResultsCollectionValueObject(
          briefResults);
      brvo.setTotalBriefResults(totalBriefResults);
      brvo.setOffset(requestParameters.getOffset());
      brvo.setRows(requestParameters.getRows());
      brvo.setMaxRowsForUser(maxRowsForUser);

      // Add the brief results to the response object (after the tabTyoe=web
      // facet count has been added to the totalBriefResults value
      allBriefResultsValuesMap.put("results", brvo);
      /******************** END PROCESS BASIC SEARCH RESULTS ****************************/

      /******************** PROCESS WEB GROUP RESULTS ***********************************/
      processWebGroupResults(intermediateResults, allBriefResultsValuesMap);

      /******************** END PROCESS WEB GROUP RESULTS *******************************/

      /******************** GET FACET COUNTS ********************************************/
      ArrayList<LinkedHashMap<String, Object>> facetResultsList = new ArrayList<LinkedHashMap<String, Object>>();
      if (qryResponse.getFacetFields() != null) {
        facetResultsList = buildFacetCounts(qryResponse.getFacetFields(),
            intermediateResults.getArchivesWebCount(),
            intermediateResults.getPresidentialWebCount());
      }

      // Combine the facet count results
      LinkedHashMap<String, Object> facetFieldsMap = new LinkedHashMap<String, Object>();
      if (facetResultsList.size() > 0) {
        facetFieldsMap.put("field", facetResultsList);
      }
      if (facetFieldsMap.size() > 0) {
        allBriefResultsValuesMap.put("facets", facetFieldsMap);
      } else {
        allBriefResultsValuesMap.put("facets", null);
      }
      /******************** END GET FACET COUNTS ****************************************/
      

      /******************** GET SPELL CHECK AND THESAURUS *******************************/
      // Add the spell-checking suggestions to the response object
      processSpellCheckingSuggestions(qryResponse, allBriefResultsValuesMap);

      // Add the thesaurus results to the response object
      processThesaurusResults(intermediateResults, allBriefResultsValuesMap);

      /******************** END GET SPELL CHECK AND THESAURUS ***************************/

      
      /******************** PROCESS SEARCH WITHIN QUERY *********************************/
      if (excecuteSearchWithinQuery) {
        processSearchWithinQuery(ancestorNaId, requestParameters.getHighlight(), opaPath,
            search_timeout, allBriefResultsValuesMap);
      }
      /******************** END PROCESS SEARCH WITHIN QUERY *****************************/

    } catch (SolrServerException e) {
      String errorId = UUID.randomUUID().toString();
      logger.error(e.getMessage()+", Error Id="+errorId, e);
      OpaRuntimeException oe=new OpaRuntimeException(e);
      oe.setErrorId(errorId);
      throw oe;
    } catch (RemoteSolrException e) {
      String errorId = UUID.randomUUID().toString();
      String errorMessage = e.getMessage()+", Error Id="+errorId;
      logger.error(errorMessage, e);
      logger.debug("creating OpaApiResponseRuntimeException for "+e+" message="+e.getMessage());
      OpaApiResponseRuntimeException ex = new OpaApiResponseRuntimeException(
          errorMessage);
      ex.setErrorMessage(errorMessage);
      ex.setAction("search");
      ex.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
      ex.setHttpStatus(HttpStatus.BAD_REQUEST);

      throw ex;
    } catch (TimeoutException te) {
    	String errorId = UUID.randomUUID().toString();
        String errorMessage = String.format(ArchitectureErrorMessageConstants.TIMEOUT_REACHED, search_timeout/1000);
        errorMessage+=" Error Id="+errorId;
        logger.error(te.getMessage()+", Error Id="+errorId, te);
        logger.debug("Caught TimeoutException. Creating OpaApiResponseRuntimeException for "+te+" message="+te.getMessage()+", Error Id="+errorId);
        OpaApiResponseRuntimeException ex = new OpaApiResponseRuntimeException(
            errorMessage);
        ex.setErrorId(errorId);
        ex.setErrorMessage(errorMessage);
        ex.setAction("search");
        ex.setErrorCode(ArchitectureErrorCodeConstants.TIMEOUT);
        ex.setHttpStatus(HttpStatus.NOT_FOUND);
        throw ex;
    } catch (Exception e) {
    	String errorId = UUID.randomUUID().toString();
    	String m="Caught generic exception"+e+" , exception message="+e.getMessage()+", error Id="+errorId;
    	logger.error(m, e);
    	String newMessage=e.getMessage()+", error Id="+errorId;
    	OpaRuntimeException oe=new OpaRuntimeException(newMessage,e);
    	oe.setErrorId(errorId);
    	throw oe;
    }

    return allBriefResultsValuesMap;

  }
  
  
  

  /**
   * Retrieve the query action from the parameters
   * 
   * @param requestParameters
   * @return
   */
  private String getQueryAction(BriefResultsRequestParameters requestParameters) {
    // Determine the action
    String queryAction = !StringUtils.isNullOrEmtpy(requestParameters
        .getAction()) ? requestParameters.getAction() : "";

    return queryAction;
  }

  /**
   * Get the ancestor NaId when search within query is expected
   * 
   * @param requestParameters
   * @param searchWithinQuery
   * @param excecuteSearchWithinQuery
   * @return
   */
  private String getAncestorNaId(
      BriefResultsRequestParameters requestParameters,
      String searchWithinQuery, boolean excecuteSearchWithinQuery) {
    String ancestorNaId = "";

    // Set Search Within Query Flag + retrieve the ancestor naId
    if (excecuteSearchWithinQuery) {

      // Extract the search within ancestorNaId
      String[] splitQuery = searchWithinQuery.split("&");
      for (int x = 0; x < splitQuery.length; x++) {
        String querySubStr = splitQuery[x].trim();
        if (querySubStr.contains("f.ancestorNaIds=")) {
          ancestorNaId = querySubStr.substring(16, querySubStr.length());
          break;
        }
      }
    }

    return ancestorNaId;
  }
  
  private String buildSearchWithinQuery(BriefResultsRequestParameters requestParameters, String query) {
    StringBuilder sb = new StringBuilder(query);
    
    SearchUtils.getHighlightedQuery(requestParameters.getHighlight(), sb);
    
    return sb.toString();
  }
  
  private String buildQuery(BriefResultsRequestParameters requestParameters, String query) {
    StringBuilder sb = new StringBuilder(query);
    
    SearchUtils.getThesaurusAndResultFields(sb, RESULT_FIELDS);
    
    SearchUtils.getQueryTabFilter(requestParameters.getTabType(), sb);
    
    return sb.toString();
  }

  /**
   * Performs the basic search specified by the query parameter.
   * 
   * @param query
   * @param opaPath
   * @param search_timeout
   * @param sUtils
   * @return The resulting query response
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws TimeoutException
   */
  private QueryResponse performSearch(String query, String opaPath,
      int search_timeout, SolrUtils sUtils) throws InterruptedException,
      ExecutionException, TimeoutException {

    logger.info(" query: " + query);

    // Build the Solr parameters
    SolrParams querySolrParams = sUtils.makeParams(opaPath, query,
        search_timeout);

    // Execute the search within search
    return SolrUtils.doSearchWithTimeout(solrServer, querySolrParams, search_timeout);
  }

  /**
   * Processes the base results into the brief results value object list
   * 
   * @param resultsList
   * @param intermediateResults
   * @param briefResults
   */
  @SuppressWarnings("unchecked")
  private void processSearchResults(SolrDocumentList resultsList,
      BriefResultsIntermediateValueObject intermediateResults,
      List<BriefResultsValueObject> briefResults) {

    for (int docNumber = 0; docNumber < resultsList.size(); docNumber++) {
      SolrDocument doc = resultsList.get(docNumber);

      String opaId = NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("opaId"), "");
      String teaser = NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("teaser"), "");

      // Retrieve the hasOnline
      boolean hasOnline = false;
      if (doc.getFieldValue("hasOnline") != null
          && (doc.getFieldValue("hasOnline").equals("true") || doc
              .getFieldValue("hasOnline").equals("TRUE"))) {
        hasOnline = true;
      } else {
        hasOnline = false;
      }
      
      boolean isOnline = false;
      if (doc.getFieldValue("isOnline") != null
          && (doc.getFieldValue("isOnline").equals("true") || doc
              .getFieldValue("isOnline").equals("TRUE"))) {
    	  isOnline = true;
      } else {
    	  isOnline = false;
      }

      // Use highlight teaser if it exists
      if (intermediateResults.getSolrHighlightingResults() != null && intermediateResults.getSolrHighlightingResults().get(opaId) != null) {
        SimpleOrderedMap<Object> highlightsMap = (SimpleOrderedMap<Object>) intermediateResults
            .getSolrHighlightingResults().get(opaId);
        if (highlightsMap.get("shortContent") != null) {
          List<String> teaserList = (ArrayList<String>) highlightsMap
              .get("shortContent");
          teaser = StringUtils.removeMarkUps((String) teaserList.get(0));
        }
      }

      // Retrieve the document brief results
      HashMap<String, ArrayList<Map<String, Object>>> solrDocumentBriefResults = new HashMap<String, ArrayList<Map<String, Object>>>();
      solrDocumentBriefResults = (HashMap<String, ArrayList<Map<String, Object>>>) intermediateResults
          .getSolrBriefResults().get(opaId);

      // Build the brief results object
      BriefResultsValueObject briefResult = new BriefResultsValueObject();
      briefResult.setDocNumber(docNumber);
      briefResult.setScore(NullableUtils.ifNullReturnDefault((Float) doc.getFieldValue("score"), 0.0f));
      briefResult.setNaId(StringUtils.removeMarkUps(NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("naId"), "")));
      briefResult.setOpaId(opaId);
      briefResult.setUrl(NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("url"), ""));
      briefResult.setIconType(NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("iconType"), ""));
      briefResult.setIsOnline(isOnline);
      briefResult.setThumbnailFile(NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("thumbnailFile"), ""));
      briefResult.setHasOnline(hasOnline);
      briefResult.setTabType(NullableUtils.ifNullReturnDefault((List<String>) doc.getFieldValue("tabType"), new ArrayList<String>()));
      briefResult.setTeaser(teaser);
      briefResult.setBriefResults(solrDocumentBriefResults);

      String objectsXML = NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("objects"),"");

      if(!"".equalsIgnoreCase(objectsXML)){
        OpaStorage storage = opaStorageFactory.createOpaStorage();
        try {
          boolean isNewObjectKeyFormat = false;
          String objectBaseKey = getObjectBaseKey(objectsXML);

          if (objectBaseKey.startsWith("/")) {
            objectBaseKey = objectBaseKey.substring(1);
          }
          if (objectBaseKey.startsWith("lz")) {
            objectBaseKey = objectBaseKey.replace("lz","live");
            isNewObjectKeyFormat = true;
          }

          briefResult.setObjectBaseKey(objectBaseKey);
          if(isNewObjectKeyFormat) {
            String baseUrl = naraBaseUrl + "catalogmedia/" + objectBaseKey;
            // replace thumbnail url with new format
            if (!"".equalsIgnoreCase(briefResult.getThumbnailFile())) {
              briefResult.setThumbnailFile(baseUrl + "/" + briefResult.getThumbnailFile());
            }
          }

        }catch(Exception e){
          logger.error("Could not get object base key! objects XML: "+objectsXML,e);
        }
      }

      briefResults.add(briefResult);
    }

  }

  private String getObjectBaseKey(String objectXML) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    String key = null;

    XPath xPath = XPathFactory.newInstance().newXPath();
    XPathExpression xPathExpression = xPath.compile("//object[1]/file[1]/@path");
    key = (String) xPathExpression.evaluate(new InputSource(new StringReader(objectXML)), XPathConstants.STRING);

    return key;
  }

  /**
   * Processes the web group results into the global results object
   * 
   * @param intermediateResults
   * @param allBriefResultsValuesMap
   */
  @SuppressWarnings("unchecked")
  private void processWebGroupResults(
      BriefResultsIntermediateValueObject intermediateResults,
      LinkedHashMap<String, Object> allBriefResultsValuesMap) {
    if (intermediateResults.getSolrWebGroupResults() != null) {
      int webDocCounter = 0;
      List<WebResultsValueObject> webResults = new ArrayList<WebResultsValueObject>();
      Set<Object> s1 = intermediateResults.getSolrWebGroupResults().keySet();
      Iterator<Object> iter1 = s1.iterator();
      while (iter1.hasNext()) {
        String webNaId = (String) iter1.next();

        if (!webNaId.equals("archivesWebCount")
            && !webNaId.equals("presidentialWebCount")) {

          // Build the web results object
          LinkedHashMap<Object, Object> webInfo = (LinkedHashMap<Object, Object>) intermediateResults
              .getSolrWebGroupResults().get(webNaId);

          String webOpaId = "";
          String webTitle = "";
          String webArea = "";
          String webAreaUrl = "";
          String webUrl = "";
          String webIconType = "";
          String webTeaser = "";

          // Extract opaId value
          if (webInfo.get("opaId") != null) {
            List<LinkedHashMap<Object, Object>> opaIdInfo = (ArrayList<LinkedHashMap<Object, Object>>) webInfo
                .get("opaId");
            if (opaIdInfo.size() > 0) {
              LinkedHashMap<Object, Object> titleLineValues = (LinkedHashMap<Object, Object>) opaIdInfo
                  .get(0);
              webOpaId = (String) titleLineValues.get("value");
            }
          }

          // Extract webTitle value
          if (webInfo.get("titleLine") != null) {
            List<LinkedHashMap<Object, Object>> webTitleInfo = (ArrayList<LinkedHashMap<Object, Object>>) webInfo
                .get("titleLine");
            if (webTitleInfo.size() > 0) {
              LinkedHashMap<Object, Object> titleLineValues = (LinkedHashMap<Object, Object>) webTitleInfo
                  .get(0);
              webTitle = (String) titleLineValues.get("value");
            }
          }

          // Extract webArea value
          if (webInfo.get("metadataArea") != null) {
            List<LinkedHashMap<Object, Object>> webAreaInfo = (ArrayList<LinkedHashMap<Object, Object>>) webInfo
                .get("metadataArea");
            if (webAreaInfo.size() > 0) {
              LinkedHashMap<Object, Object> metadataAreaValues = (LinkedHashMap<Object, Object>) webAreaInfo
                  .get(0);
              webArea = (String) metadataAreaValues.get("value");
            }
          }

          // Extract webAreaUrl value
          if (webInfo.get("webAreaUrl") != null) {
            List<LinkedHashMap<Object, Object>> webAreaUrlInfo = (ArrayList<LinkedHashMap<Object, Object>>) webInfo
                .get("webAreaUrl");
            if (webAreaUrlInfo.size() > 0) {
              LinkedHashMap<Object, Object> metadataAreaValues = (LinkedHashMap<Object, Object>) webAreaUrlInfo
                  .get(0);
              webAreaUrl = (String) metadataAreaValues.get("value");
            }
          }

          // Extract webUrl value
          if (webInfo.get("url") != null) {
            List<LinkedHashMap<Object, Object>> webUrlInfo = (ArrayList<LinkedHashMap<Object, Object>>) webInfo
                .get("url");
            if (webUrlInfo.size() > 0) {
              LinkedHashMap<Object, Object> metadataAreaValues = (LinkedHashMap<Object, Object>) webUrlInfo
                  .get(0);
              webUrl = (String) metadataAreaValues.get("value");
            }
          }

          // Extract webIconType value
          if (webInfo.get("iconType") != null) {
            List<LinkedHashMap<Object, Object>> webIconTypeInfo = (ArrayList<LinkedHashMap<Object, Object>>) webInfo
                .get("iconType");
            if (webIconTypeInfo.size() > 0) {
              LinkedHashMap<Object, Object> metadataAreaValues = (LinkedHashMap<Object, Object>) webIconTypeInfo
                  .get(0);
              webIconType = (String) metadataAreaValues.get("value");
            }
          }

          // Extract webTeaser value
          if (webInfo.get("teaser") != null) {
            List<LinkedHashMap<Object, Object>> teaserInfo = (ArrayList<LinkedHashMap<Object, Object>>) webInfo
                .get("teaser");
            if (teaserInfo.size() > 0) {
              LinkedHashMap<Object, Object> metadataAreaValues = (LinkedHashMap<Object, Object>) teaserInfo
                  .get(0);
              webTeaser = (String) metadataAreaValues.get("value");
            }
          }

          WebResultsValueObject webResult = new WebResultsValueObject();
          webResult.setDocNumber(webDocCounter);
          webResult.setOpaId(webOpaId);
          webResult.setTitle(webTitle);
          webResult.setWebArea(webArea);
          webResult.setWebAreaUrl(webAreaUrl);
          webResult.setUrl(webUrl);
          webResult.setIconType(webIconType);
          webResult.setTeaser(webTeaser);
          webResults.add(webResult);
          webDocCounter++;

        } else if (webNaId.equals("archivesWebCount")) {
          intermediateResults.setArchivesWebCount((Integer) intermediateResults
              .getSolrWebGroupResults().get(webNaId));
        } else if (webNaId.equals("presidentialWebCount")) {
          intermediateResults
              .setPresidentialWebCount((Integer) intermediateResults
                  .getSolrWebGroupResults().get(webNaId));
        }

      }

      // Add the web group results to the response object
      WebResultsCollectionValueObject wrvo = new WebResultsCollectionValueObject(
          webResults);
      wrvo.setTotalWebResults(intermediateResults.getArchivesWebCount()
          + intermediateResults.getPresidentialWebCount());
      wrvo.setOffset(0);
      wrvo.setRows(webDocCounter);
      allBriefResultsValuesMap.put("webPages", wrvo);
    }

  }

  /**
   * Executes the search within query action and updates the result object
   * 
   * @param ancestorNaId
   * @param highlight
   * @param opaPath
   * @param search_timeout
   * @param allBriefResultsValuesMap
   * @throws SolrServerException
   */
  @SuppressWarnings("unchecked")
  private void processSearchWithinQuery(String ancestorNaId, boolean highlight,
      String opaPath, int search_timeout,
      LinkedHashMap<String, Object> allBriefResultsValuesMap)
      throws SolrServerException {
    List<SearchWithinValueObject> searchWithinResults = new ArrayList<SearchWithinValueObject>();

    // Perform an additional search to retrieve the search within result
   
    String searchWithinQuery = String.format(SEARCH_WITHIN_QUERY, ancestorNaId);

    if (highlight) {
      searchWithinQuery = searchWithinQuery
          + HIGHLIGHT;
    }

    logger.info(" searchWithinQuery: " + searchWithinQuery);

    // Build the Solr parameters
    SolrParams searchWithinSolrParams = sUtils.makeParams(opaPath,
        searchWithinQuery, search_timeout);

    // Execute the search within search
    QueryResponse searchWithinQryResponse = solrServer
        .query(searchWithinSolrParams);

    // Retrieve the web query response time
    double searchWithinQueryTime = searchWithinQryResponse.getQTime();
    logger.info(" searchWithinQueryTime: " + searchWithinQueryTime);

    // Extract the search within results
    SolrDocumentList searchWithinResultsList = searchWithinQryResponse
        .getResults();

    // Extract the brief results
    SimpleOrderedMap<Object> searchWithinSolrBriefResults = new SimpleOrderedMap<Object>();
    searchWithinSolrBriefResults = (SimpleOrderedMap<Object>) searchWithinQryResponse
        .getResponse().get("briefResults");

    // Extract the highlighting results
    SimpleOrderedMap<Object> searchWithinSolrHighlightingResults = new SimpleOrderedMap<Object>();
    if (highlight) {
      searchWithinSolrHighlightingResults = (SimpleOrderedMap<Object>) searchWithinQryResponse
          .getResponse().get("highlighting");
    }

    if (searchWithinResultsList.size() > 0) {
      SolrDocument doc = searchWithinResultsList.get(0);

      Float score = NullableUtils.ifNullReturnDefault((Float) doc.getFieldValue("score"), 0.0f);
      String naId = StringUtils.removeMarkUps(NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("naId"), ""));
      String opaId = NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("opaId"), "");
      String title = StringUtils.removeMarkUps(NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("title"), ""));
      String url = NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("url"), "");
      String iconType = NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("iconType"), "");
      String thumbnailFile = NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("thumbnailFile"), "");
      List<String> tabType = NullableUtils.ifNullReturnDefault((List<String>) doc.getFieldValue("tabType"), new ArrayList<String>()); 
      String teaser = NullableUtils.ifNullReturnDefault((String) doc.getFieldValue("teaser"), "");
      List<String> ancestorNaIds = NullableUtils.ifNullReturnDefault((ArrayList<String>) doc.getFieldValue("ancestorNaIds"), new ArrayList<String>());

      // Retrieve the hierachy
      String parentNaId = "";
      String label = "";
      String hierachy = "";
      boolean doTopLevelSearch = false;
      if (ancestorNaIds.size() > 0) {

        parentNaId = StringUtils.removeMarkUps(NullableUtils.ifNullReturnDefault((String) doc
            .getFieldValue("parentNaId"), ""));
        String parentLevel = NullableUtils.ifNullReturnDefault((String) doc
            .getFieldValue("parentLevel"), "");
        String parentTitle = NullableUtils.ifNullReturnDefault((String) doc
            .getFieldValue("parentTitle"), "");

        if (labelMap.containsKey(parentLevel)) {
          label = labelMap.get(parentLevel);
        } else {
          label = parentLevel;
        }

        hierachy = label + ":" + parentTitle;

        // Retrieve the top Level
        if (ancestorNaIds.size() > 1) {
          doTopLevelSearch = true;
        }
      }

      // Retrieve the hasOnline
      boolean hasOnline = false;
      if (doc.getFieldValue("hasOnline") != null
          && (doc.getFieldValue("hasOnline").equals("true") || doc
              .getFieldValue("hasOnline").equals("TRUE")))
        hasOnline = true;
      else
        hasOnline = false;

      // Use highlight teaser if it exists
      if (searchWithinSolrHighlightingResults.get(opaId) != null) {
        SimpleOrderedMap<Object> searchWithinHighlightsMap = (SimpleOrderedMap<Object>) searchWithinSolrHighlightingResults
            .get(opaId);
        if (searchWithinHighlightsMap.get("shortContent") != null) {
          List<String> teaserList = (ArrayList<String>) searchWithinHighlightsMap
              .get("shortContent");
          // teaser = teaserList.get(0);
          teaser = StringUtils.removeMarkUps((String) teaserList.get(0));
        }
      }

      if (doTopLevelSearch) {

        searchWithinQuery = "?action=searchWithin&q=naId:" + parentNaId
            + "&apiType=iapi";

        logger.info(" searchWithinQuery-topLevel: " + searchWithinQuery);

        // Build the Solr parameters
        searchWithinSolrParams = sUtils.makeParams(opaPath, searchWithinQuery,
            -1);

        // Execute the search within search
        searchWithinQryResponse = solrServer.query(searchWithinSolrParams);

        // Extract the search within results
        searchWithinResultsList = searchWithinQryResponse.getResults();

        if (searchWithinResultsList.size() > 0) {

          doc = searchWithinResultsList.get(0);

          // Retrieve the parentLevel
          String topParentLevel = "";
          if (doc.getFieldValue("parentLevel") != null)
            topParentLevel = (String) doc.getFieldValue("parentLevel");

          // Retrieve the parentTitle
          String topParentTitle = "";
          if (doc.getFieldValue("parentTitle") != null)
            topParentTitle = (String) doc.getFieldValue("parentTitle");

          String topParentLabel = "";
          if (labelMap.containsKey(topParentLevel)) {
            topParentLabel = labelMap.get(topParentLevel);
          } else {
            topParentLabel = topParentLevel;
          }

          hierachy = hierachy + ", " + topParentLabel + ":" + topParentTitle;

        }
      }

      // Retrieve the document brief results
      SearchWithinValueObject searchWithinResult = new SearchWithinValueObject();

      HashMap<String, ArrayList<Map<String, Object>>> solrDocumentBriefResults = new HashMap<String, ArrayList<Map<String, Object>>>();
      solrDocumentBriefResults = (HashMap<String, ArrayList<Map<String, Object>>>) searchWithinSolrBriefResults
          .get(opaId);

      // Build the brief results object
      searchWithinResult.setDocNumber(0);
      searchWithinResult.setScore(score);
      searchWithinResult.setNaId(naId);
      searchWithinResult.setOpaId(opaId);
      searchWithinResult.setTitle(title);
      searchWithinResult.setHierachy(hierachy);
      searchWithinResult.setUrl(url);
      searchWithinResult.setIconType(iconType);
      searchWithinResult.setThumbnailFile(thumbnailFile);
      searchWithinResult.setHasOnline(hasOnline);
      searchWithinResult.setTabType(tabType);
      searchWithinResult.setTeaser(teaser);
      searchWithinResult.setBriefResults(solrDocumentBriefResults);
      searchWithinResults.add(searchWithinResult);
    }

    logger.info(" Total Search Within Results: " + searchWithinResults.size());

    // Add the search within results to the response object
    if (searchWithinResults.size() > 0) {
      allBriefResultsValuesMap.put("searchWithin", searchWithinResults);
    } else {
      allBriefResultsValuesMap.put("searchWithin", null);
    }

  }

  /**
   * Add the spell checking suggestions to the result object
   * 
   * @param qryResponse
   * @param allBriefResultsValuesMap
   */
  private void processSpellCheckingSuggestions(QueryResponse qryResponse,
      LinkedHashMap<String, Object> allBriefResultsValuesMap) {
    if (qryResponse.getSpellCheckResponse() != null) {
      SpellCheckResponse spellCheckResponse = qryResponse
          .getSpellCheckResponse();

      if (spellCheckResponse.getCollatedResults() != null
          && spellCheckResponse.getCollatedResults().size() > 0) {
        List<Collation> collationList = spellCheckResponse.getCollatedResults();
        LinkedHashMap<String, Object> spellingResultsMap = new LinkedHashMap<String, Object>();
        if (collationList != null && collationList.size() > 0) {
          spellingResultsMap = buildSpellingResults(collationList);
        }
        if (spellingResultsMap != null && spellingResultsMap.size() > 0) {
          allBriefResultsValuesMap.put("spellingResults", spellingResultsMap);
          logger.info(" Has Spelling Results: TRUE");
        } else {
          allBriefResultsValuesMap.put("spellingResults", null);
          logger.info(" Has Spelling Results: FALSE");
        }
      }
    }
  }

  /**
   * Includes the thesaurus results in the result object
   * 
   * @param intermediateResults
   * @param allBriefResultsValuesMap
   */
  private void processThesaurusResults(BriefResultsIntermediateValueObject intermediateResults,
      LinkedHashMap<String, Object> allBriefResultsValuesMap) {
    if (intermediateResults.getSolrThesaurusMap() != null
        && intermediateResults.getSolrThesaurusMap().size() > 0) {
      boolean containsTerms = false;
      List<Object> termsList = new ArrayList<Object>();
      LinkedHashMap<String, Object> thesaurusMap = new LinkedHashMap<String, Object>();
      Set<String> s2 = intermediateResults.getSolrThesaurusMap().keySet();
      Iterator<String> iter2 = s2.iterator();
      while (iter2.hasNext()) {
        String thesaurusTerm = iter2.next();
        LinkedHashMap<String, List<String>> solrTermsMap = intermediateResults
            .getSolrThesaurusMap().get(thesaurusTerm);
        LinkedHashMap<String, Object> thesaurusTermsMap = new LinkedHashMap<String, Object>();
        thesaurusTermsMap.put("@name", thesaurusTerm);

        // Extract the broader terms
        if (solrTermsMap != null && solrTermsMap.containsKey("broaderTerms")) {
          List<String> broaderTerms = new ArrayList<String>();
          broaderTerms = solrTermsMap.get("broaderTerms");
          LinkedHashMap<String, List<String>> termsValueMap = new LinkedHashMap<String, List<String>>();
          if (broaderTerms != null && broaderTerms.size() > 0) {
            termsValueMap.put("val", broaderTerms);
            thesaurusTermsMap.put("broader", termsValueMap);
            containsTerms = true;
          }
        }

        // Extract the narrower terms
        if (solrTermsMap != null && solrTermsMap.containsKey("narrowerTerms")) {
          List<String> narrowerTerms = new ArrayList<String>();
          narrowerTerms = solrTermsMap.get("narrowerTerms");
          LinkedHashMap<String, List<String>> termsValueMap = new LinkedHashMap<String, List<String>>();
          if (narrowerTerms != null && narrowerTerms.size() > 0) {
            termsValueMap.put("val", narrowerTerms);
            thesaurusTermsMap.put("narrower", termsValueMap);
            containsTerms = true;
          }
        }

        // Extract the related terms
        if (solrTermsMap != null && solrTermsMap.containsKey("relatedTerms")) {
          List<String> relatedTerms = new ArrayList<String>();
          relatedTerms = solrTermsMap.get("relatedTerms");
          LinkedHashMap<String, List<String>> termsValueMap = new LinkedHashMap<String, List<String>>();
          if (relatedTerms != null && relatedTerms.size() > 0) {
            termsValueMap.put("val", relatedTerms);
            thesaurusTermsMap.put("related", termsValueMap);
            containsTerms = true;
          }
        }
        if (containsTerms) {
          termsList.add(thesaurusTermsMap);
        }
      }
      if (containsTerms) {
        thesaurusMap.put("term", termsList);
        allBriefResultsValuesMap.put("thesaurus", thesaurusMap);
        logger.info(" Has Thesaurus Results: TRUE");
      } else {
        allBriefResultsValuesMap.put("thesaurus", null);
        logger.info(" Has Thesaurus Results: FALSE");
      }
    }
  }
  
  private void initializeLabelMap() {
    labelMap.put("collection", "Collection");
    labelMap.put("recordGroup", "Record Group");
    labelMap.put("series", "Series");
    labelMap.put("fileUnit", "File Unit");
  }

  /**
   * Method to construct the spelling results response
   * 
   * @param collationList
   *          list of spelling suggestions returned from solr
   * @return LinkedHashMap<String, Object> of spelling suggestions
   */
  private LinkedHashMap<String, Object> buildSpellingResults(
      List<Collation> collationList) {
    LinkedHashMap<String, Object> spellingResultsMap = new LinkedHashMap<String, Object>();
    ArrayList<Object> spellingResultsList = new ArrayList<Object>();
    spellingResultsMap.put("@total", collationList.size());
    for (Collation c : collationList) {
      spellingResultsList.add(c.getCollationQueryString());
    }
    spellingResultsMap.put("spellingResult", spellingResultsList);

    return spellingResultsMap;
  }

  /**
   * @param fflist
   *          list of facet fields & counts returned from solr
   * @return ArrayList<LinkedHashMap<String, Object>> of facet fields & counts
   */
  private ArrayList<LinkedHashMap<String, Object>> buildFacetCounts(
      List<FacetField> fflist, int archivesWebCount, int presidentialWebCount) {
    int totalWebTabTypeCount = archivesWebCount + presidentialWebCount;
    ArrayList<LinkedHashMap<String, Object>> facetResultsList = new ArrayList<LinkedHashMap<String, Object>>();
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
      if (facetLabel.equals("tabType")) {
        LinkedHashMap<String, Object> facetValuesMap = new LinkedHashMap<String, Object>();
        facetValuesMap.put("@name", "web");
        facetValuesMap.put("@count", Integer.toString(totalWebTabTypeCount));
        facetValuesList.add(facetValuesMap);
      }
      if (facetLabel.equals("oldScope")) {
        LinkedHashMap<String, Object> facetValuesMap1 = new LinkedHashMap<String, Object>();
        facetValuesMap1.put("@name", "archives.gov");
        facetValuesMap1.put("@count", Integer.toString(archivesWebCount));
        facetValuesList.add(facetValuesMap1);
        LinkedHashMap<String, Object> facetValuesMap2 = new LinkedHashMap<String, Object>();
        facetValuesMap2.put("@name", "presidential");
        facetValuesMap2.put("@count", Integer.toString(presidentialWebCount));
        facetValuesList.add(facetValuesMap2);
      }
      if (facetValuesList.size() > 0) {
        facetResults.put("@name", facetLabel);
        facetResults.put("v", facetValuesList);
      }
      if (facetResults.size() > 0) {
        facetResultsList.add(facetResults);
      }
    }
    return facetResultsList;
  }

}
