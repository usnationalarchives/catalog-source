package gov.nara.opa.api.utils;

import gov.nara.opa.api.search.Search;
import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.SingletonServices;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

@Component
@Scope("prototype")
public class SearchUtils {

	private static final String HIGHLIGHT_SETTINGS = "&hl=true&hl.fl=title,creators,location,teaser,naId,shortContent";

	private static final String FULL_HIGHLIGHT_SETTINGS = "&hl=true&hl.fl=*";

	private static final String TAB_FILTER = "&filter=(tabType:all%20and%20not(source:web))";

	private static final String ONLINE_TAB_FILTER = "&filter=(tabType:online%20and%20not(source:web))";

	private static final String CONTENT_DETAIL_QUERY = "?contentDetailID=%1$s&resultFields=naId,opaId,title,source,sourceType,"
			+ "description,highlightedDescriptionXml,authority,"
			+ "highlightedAuthorityXml,objects"
			+ "&f.shortContent.hl.fragsize=0";

	private static OpaLogger logger = OpaLogger.getLogger(SearchUtils.class);

	@Autowired
	private GetLoadBalancedSolrServer getLoadBalancedSolrServer;

	@Autowired
	private ConfigurationService configurationService;

	private LoadBalancedHttpSolrServer solrServer;

	/**
	 * Retrieve the opaId for a given naId/objectId
	 * 
	 * @param apiType
	 *          iapi (internal) or api (public)
	 * @param naId
	 *          NARA ID
	 * @param objectId
	 *          Object ID
	 * @return page number
	 */

	public String getOpaId(String apiType, String naId, String objectId) {
		String query = "?q=naId:" + naId;
		if (!StringUtils.isNullOrEmtpy(objectId)) {
			query = query + "%20AND%20objectId:" + objectId;
		}
		query = query + "&resultFields=opaId";
		String opaPath = apiType + "/" + Constants.API_VERS_NUM;
		String opaId = "";

		try {

			logger.info(" Search By ID Query: " + query);

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

			// Extract the opaId value
			if (resultsList != null && resultsList.size() > 0) {
				SolrDocument doc = resultsList.get(0);

				if (doc.getFieldValue("opaId") != null)
					opaId = (String) doc.getFieldValue("opaId");

			} else {
				return "";
			}

		} catch (SolrServerException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return opaId;
	}

	public static boolean isInWhiteList(String parameterName) {

		boolean inWhiteList = SingletonServices.SOLR_FIELDS_INTERNAL_WHITE_LIST
				.contains(parameterName);
		if (inWhiteList) {
			return true;
		}

		//Evaluate 'starts with'
		for(Object prefixObj : SingletonServices.SOLR_FIELDS_STARTS_WITH_LIST.toArray()) {
			String prefix = prefixObj.toString();

			if(parameterName.startsWith(prefix)) {
				return true;
			}
		}

		// TODO: add "ends with" list
		//to avoid having to create a new list for testing, we'll use STARTS_WITH
		for(Object suffixObj : SingletonServices.SOLR_FIELDS_STARTS_WITH_LIST.toArray()) {
			String suffix = suffixObj.toString();

			if(parameterName.endsWith(suffix)) {
				return SingletonServices.DAS_WHITE_LIST.contains(parameterName.replace(suffix,""));
			}
		}

		return SingletonServices.DAS_WHITE_LIST.contains(parameterName);
	}

	/**
	 * Adds the highlight settings to the query if requested
	 * 
	 * @param highlight
	 * @param query
	 * @return The highlighted query string
	 */
	public static String getHighlightedQuery(boolean highlight, String query) {
		StringBuilder sb = new StringBuilder(query);

		if (highlight && !query.contains(HIGHLIGHT_SETTINGS)) {
			sb.append(HIGHLIGHT_SETTINGS);
		}

		return sb.toString();
	}

	public static String getHighlightedQuery(boolean highlight, String query, boolean fullHighlight) {
		StringBuilder sb = new StringBuilder(query);

		if(!fullHighlight) {
			if (highlight && !query.contains(HIGHLIGHT_SETTINGS)) {
				sb.append(HIGHLIGHT_SETTINGS);
			}
		} else {
			if (highlight && !query.contains(FULL_HIGHLIGHT_SETTINGS)) {
				sb.append(FULL_HIGHLIGHT_SETTINGS);
			}

		}

		return sb.toString();    
	}

	public static void getHighlightedQuery(boolean highlight, StringBuilder query) {
		if (highlight && !query.toString().contains(HIGHLIGHT_SETTINGS)) {
			query.append(HIGHLIGHT_SETTINGS);
		}
	}

	public static String getThesaurusAndResultFields(String query, String resultFields) {
		return query + "&thesaurus=true" + resultFields;
	}

	public static void getThesaurusAndResultFields(StringBuilder query, String resultFields) {
		query.append("&thesaurus=true" + resultFields);
	}

	/**
	 * Adds tab filter settings to the query string
	 * 
	 * @param queryTabType
	 * @param query
	 * @return The query string with the tab filter settings
	 */
	public static String getQueryTabFilter(String queryTabType, String query) {
		String resultQuery = query;

		// Exclude Web Results if tabType=all
		if (!StringUtils.isNullOrEmtpy(queryTabType)
				&& queryTabType.equalsIgnoreCase("all")) {
			resultQuery = query + TAB_FILTER;
		}

		if (!StringUtils.isNullOrEmtpy(queryTabType)) {
			if(queryTabType.equalsIgnoreCase(TabTypeConstants.TAB_TYPE_ALL)) {
				resultQuery = query + TAB_FILTER;
			} else if (queryTabType.equalsIgnoreCase(TabTypeConstants.TAB_TYPE_ONLINE)) {
				resultQuery = query + ONLINE_TAB_FILTER;
			}
		}

		return resultQuery;
	}

	public static void getQueryTabFilter(String queryTabType, StringBuilder query) {
		// Exclude Web Results if tabType=all
		if (!StringUtils.isNullOrEmtpy(queryTabType)) {
			if(queryTabType.equalsIgnoreCase(TabTypeConstants.TAB_TYPE_ALL)) {
				query.append(TAB_FILTER);
			} else if (queryTabType.equalsIgnoreCase(TabTypeConstants.TAB_TYPE_ONLINE)) {
				query.append(ONLINE_TAB_FILTER);
			}
		}
	}

	/**
	 * Determines if the number of rows searched is allowed for the user type
	 * @param offset
	 * @param rows
	 * @param accountType
	 * @return
	 * true if the user is allowed to page over the requested rowcount
	 */
	public boolean isRowcountAllowedForUser(int offset, int rows, String accountType) {
		return isRowcountAllowedForUser(offset, rows, accountType, 0, false);
	}

	public boolean isRowcountAllowedForUser(int offset, int rows, String accountType, int publicMaxRows, boolean isPublicApi) {
		int maxRows = (publicMaxRows > 0 ? publicMaxRows : configurationService.getSearchLimitForUser(accountType));

		if ( !isPublicApi ) {
			return (offset + rows > maxRows);
		} else {
			return rows > maxRows;
		}
	}

	public AspireObject getObjects(String naId) {
		String query = String.format(CONTENT_DETAIL_QUERY, naId);
		solrServer = getLoadBalancedSolrServer.getServer();

		SolrUtils sUtils = new SolrUtils();
		SolrParams solrParams = sUtils.makeParams(Constants.INTERNAL_API_PATH, query, -1);

		AspireObject objectsXmlObject = new AspireObject("objects");

		try {
			QueryResponse qryResponse = solrServer.query(solrParams);
			SolrDocumentList resultsList = qryResponse.getResults();
			if (resultsList.size() > 0) {
				SolrDocument doc = resultsList.get(0);
				if (doc.getFieldValue("source") != null) {
					String source = (String) doc.getFieldValue("source");
					if (source.equals("holdings")) {
						String description = (String) doc.getFieldValue("description");
						if (description.startsWith("<itemAv") || description.startsWith("<item")) {
							objectsXmlObject.add("level", "Item");
						} if (description.startsWith("<series")) {
							objectsXmlObject.add("level", "Series");
						} if (description.startsWith("<collection")) {
							objectsXmlObject.add("level", "Collection");
						} if (description.startsWith("<fileUnit")) {
							objectsXmlObject.add("level", "File Unit");
						} if (description.startsWith("<recordGroup")) {
							objectsXmlObject.add("level", "Record Group");
						}
					} else {
						return null;
					}
				}

				boolean hasObjects = false;

				if (doc.getFieldValue("objects") != null) {

					String objectsXml = (String) doc.getFieldValue("objects");

					try {
						if (objectsXml != null && !objectsXml.equals("")) {
							objectsXmlObject.loadXML(new StringReader(objectsXml));
							List<AspireObject> objectList = objectsXmlObject
									.getAll("objects").get(0).getAll("object");
							for (int i = 0; i < objectList.size(); i++) {
								hasObjects = true;
							}
						}
					} catch (AspireException e) {
						logger.error(e.getMessage(), e);
						throw new OpaRuntimeException(e);
					} finally {
						try {
							objectsXmlObject.close();
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
							throw new OpaRuntimeException(e);
						}
					}
				}
				objectsXmlObject.add("hasObjects", hasObjects);
				return objectsXmlObject;
			}
		} catch (SolrServerException e) {
			logger.error(e.getMessage(), e);
		} catch (AspireException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
