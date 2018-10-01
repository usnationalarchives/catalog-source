package gov.nara.opa.api.services.impl.search;

import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.api.services.search.StatisticsService;
import gov.nara.opa.api.validation.search.StatisticsRequestParameters;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatisticsServiceImpl implements StatisticsService {

  private static OpaLogger logger = OpaLogger
      .getLogger(StatisticsServiceImpl.class);

  @Autowired
  private GetLoadBalancedSolrServer getLoadBalancedSolrServer;

  LoadBalancedHttpSolrServer solrServer;

  private String query = "?q=type%3Adescription&resultFields=naId,opaId,title,seriesCount,"
      + "fileUnitCount,itemCount,childCount&group=true&group.limit=10&"
      + "group.field=level&sort=childCount%20desc&facet=true&facet.field=level&briefresults=false";

  @Override
  public LinkedHashMap<String, Object> getStatistics(
      StatisticsRequestParameters requestParameters, String opaPath) {
    LinkedHashMap<String, Object> statisticsMap = new LinkedHashMap<String, Object>();
    LinkedHashMap<String, Integer> facetResults = new LinkedHashMap<String, Integer>();

    try {
      /**********************************************************************************/
      // START: Using Solr Software Load Balancing
      solrServer = getLoadBalancedSolrServer.getServer();
      // END: Using Solr Software Load Balancing
      /**********************************************************************************/

      logger.info("statisticsQuery:   " + query);

      // Build the Solr parameters
      SolrUtils sUtils = new SolrUtils();
      SolrParams solrParams = sUtils.makeParams(opaPath, query, -1);

      // Execute the search
      QueryResponse qryResponse = solrServer.query(solrParams);

      // Retrieve the facet results
      if (qryResponse.getFacetFields() != null) {
        facetResults = buildFacetCounts(qryResponse.getFacetFields());
      }

      // Process each group
      LinkedHashMap<String, Object> groupMap = new LinkedHashMap<String, Object>();
      GroupResponse response = qryResponse.getGroupResponse();
      List<GroupCommand> groupCommands = response.getValues();
      for (int i = 0; i < groupCommands.size(); i++) {
        GroupCommand groupCommand = groupCommands.get(i);

        List<Group> results = groupCommand.getValues();
        for (int j = 0; j < results.size(); j++) {

          LinkedHashMap<String, Object> subGroupMap = new LinkedHashMap<String, Object>();
          Group result = results.get(j);
          String groupVal = result.getGroupValue();

          // Determine which sub counts to retrieve
          boolean getSeriesCount = false;
          boolean getFileUnitCount = false;
          boolean getItemCount = false;

          // An item does not have any children
          // File Unit can have item as children
          // Series can have fileunit and item as children
          // Record group can have series and item as children
          // Collection can have series and item as children

          if (groupVal.equals("fileunit")) {
            getItemCount = true;
          } else if (groupVal.equals("series")) {
            getFileUnitCount = true;
            getItemCount = true;
          } else if ((groupVal.equals("recordgroup"))
              || (groupVal.equals("collection"))) {
            getSeriesCount = true;
            // getItemCount = true;
          }

          subGroupMap.put("count", facetResults.get(groupVal));
          List<Object> titleList = new ArrayList<Object>();
          SolrDocumentList docList = result.getResult();
          for (int k = 0; k < docList.size(); k++) {
            SolrDocument doc = docList.get(k);

            LinkedHashMap<String, Object> countsMap = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, Integer> subCountsMap = new LinkedHashMap<String, Integer>();
            String naId = "";
            String title = "";
            Integer childCount = 0;
            Integer seriesCount = 0;
            Integer fileUnitCount = 0;
            Integer itemCount = 0;

            // Retrieve the naId
            if (doc.getFieldValue("naId") != null)
              naId = StringUtils.removeMarkUps((String) doc
                  .getFieldValue("naId"));
            countsMap.put("naId", naId);

            // Retrieve the title
            if (doc.getFieldValue("title") != null) {
              title = StringUtils.removeMarkUps((String) doc
                  .getFieldValue("title"));
            }
            countsMap.put("title", title);

            // Retrieve the total children count
            if (doc.getFieldValue("childCount") != null) {
              childCount = (Integer) doc.getFieldValue("childCount");
              countsMap.put("total", childCount);
            }

            // Retrieve the series count
            if (getSeriesCount && doc.getFieldValue("seriesCount") != null) {
              seriesCount = (Integer) doc.getFieldValue("seriesCount");
              subCountsMap.put("seriesCount", seriesCount);
            }

            // Retrieve the file unit count
            if (getFileUnitCount && doc.getFieldValue("fileUnitCount") != null) {
              fileUnitCount = (Integer) doc.getFieldValue("fileUnitCount");
              subCountsMap.put("fileUnitCount", fileUnitCount);
            }

            // Retrieve the item count
            if (getItemCount && doc.getFieldValue("itemCount") != null) {
              itemCount = (Integer) doc.getFieldValue("itemCount");
              subCountsMap.put("itemCount", itemCount);
            }

            if (subCountsMap.size() > 0) {
              countsMap.put("subCounts", subCountsMap);
            } else {
              countsMap.put("subCounts", null);
            }
            titleList.add(countsMap);
          }
          subGroupMap.put("subGroup", titleList);
          groupMap.put(groupVal, subGroupMap);
        }
      }

      statisticsMap.put("statistics", groupMap);

    } catch (SolrServerException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    return statisticsMap;
  }

  /**
   * @param fflist
   *          list of facet fields & counts returned from solr
   * @return ArrayList<LinkedHashMap<String, Object>> of facet fields & counts
   */
  private LinkedHashMap<String, Integer> buildFacetCounts(
      List<FacetField> fflist) {
    LinkedHashMap<String, Integer> facetResults = new LinkedHashMap<String, Integer>();
    for (FacetField ff : fflist) {
      List<Count> counts = ff.getValues();
      for (Count c : counts) {
        String facetValue = c.getName();
        Integer facetCount = new Integer((int) c.getCount());
        facetResults.put(facetValue, facetCount);
      }
    }
    return facetResults;
  }

}
