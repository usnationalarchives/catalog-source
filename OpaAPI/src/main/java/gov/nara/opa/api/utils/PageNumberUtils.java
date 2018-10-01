package gov.nara.opa.api.utils;

import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PageNumberUtils {

  private static OpaLogger logger = OpaLogger.getLogger(PageNumberUtils.class);

  @Autowired
  private GetLoadBalancedSolrServer getLoadBalancedSolrServer;

  private LoadBalancedHttpSolrServer solrServer;

  /**
   * Retrieve the page number for a given naId/objectId
   * 
   * @param apiType
   *          iapi (internal) or api (public)
   * @param naId
   *          NARA ID
   * @param objectId
   *          Object ID
   * @return page number
   */

  public int getPageNumber(String apiType, String naId, String objectId) {
    String query = "?action=search&q=parentDescriptionNaId:" + naId + "%20AND%20objectId:"
        + objectId + "&resultFields=objectSortNum";

    String opaPath = apiType + "/" + Constants.API_VERS_NUM;
    int objectSortNum = 0;

    try {

      logger.info(" Get Page Number Query: " + query);

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

      // Extract the object sort number value
      if (resultsList != null && resultsList.size() > 0) {
        SolrDocument doc = resultsList.get(0);

        if (doc.getFieldValue("objectSortNum") != null)
          objectSortNum = (Integer) doc.getFieldValue("objectSortNum");
      } else {
        return 0;
      }

    } catch (SolrServerException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    return objectSortNum;
  }

  public int getPageNumberOrig(String apiType, String naId, String objectId) {
    String query = "?action=search&q=naId:" + naId + "&apiType=iapi"
        + "&resultFields=objects";
    String opaPath = apiType + "/" + Constants.API_VERS_NUM;
    int pageNumber = 0;

    try {

      logger.info(" Get PageNumber Query: " + query);

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

      // Process search results
      if (resultsList != null && resultsList.size() > 0) {
        SolrDocument doc = resultsList.get(0);

        // Retrieve the objects
        String objectsXml = "";
        if (doc.getFieldValue("objects") != null)
          objectsXml = (String) doc.getFieldValue("objects");

        // Calculate the pageNumber
        if (objectsXml != null && !objectsXml.equals("")) {
          pageNumber = processObjectsXml(naId, objectId, objectsXml);
        } else {
          return 0;
        }
      } else {
        return 0;
      }

    } catch (SolrServerException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    return pageNumber;
  }

  public int processObjectsXml(String naId, String objectId, String objectsXml) {
    AspireObject objectsXmlObject = new AspireObject("objects");

    try {
      objectsXmlObject.loadXML(new StringReader(objectsXml));
      List<AspireObject> objectList = objectsXmlObject.getAll("objects").get(0)
          .getAll("object");
      for (int i = 0; i < objectList.size(); i++) {
        AspireObject objectIdObject = objectList.get(i);
        String objectsXmlObjectId = objectIdObject.getAttribute("id");
        if (objectId.equals(objectsXmlObjectId)) {
          return (i + 1);
        }
      }
    } catch (AspireException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    } finally {
      try {
        objectsXmlObject.close();
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
        // throw new OpaRuntimeException(e);
      }
    }

    return 0;
  }

  public boolean isValidNaId(String apiType, String naId) {
    String query = "?action=search&q=naId:" + naId
        + "&resultFields=naId&rows=1";
    String opaPath = apiType + "/" + Constants.API_VERS_NUM;

    try {

      logger.info(" Is Valid NaId Query: " + query);

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

      // Process search results
      if (resultsList != null && resultsList.size() > 0) {
        return true;
      }

    } catch (SolrServerException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    return false;
  }

}
