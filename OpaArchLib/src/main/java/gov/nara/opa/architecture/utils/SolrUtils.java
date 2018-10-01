package gov.nara.opa.architecture.utils;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.solr.SolrGateway;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.springframework.stereotype.Component;

/**
 * @author gsofizade
 * @date Jul 31, 2014
 * 
 */
@Component
public class SolrUtils {

	private static OpaLogger logger = OpaLogger.getLogger(SolrUtils.class);

	private final List<String> NON_SPLITTABLE_VALUE_PARAMS = new ArrayList<String>();
	{
		NON_SPLITTABLE_VALUE_PARAMS.add("range\\(\\d+,\\s*\\d+\\)");
	}

	private final List<String> NON_SPLITTABLE_PARAMS = new ArrayList<String>();
	{
		NON_SPLITTABLE_PARAMS.add("naIds=\\s*");
		NON_SPLITTABLE_PARAMS.add("parentDescriptionNaIds=\\s*");
		NON_SPLITTABLE_PARAMS.add("resultTypes=\\s*");
		NON_SPLITTABLE_PARAMS.add("objectIds=\\s*");
		NON_SPLITTABLE_PARAMS.add("resultFields=\\s*");
		NON_SPLITTABLE_PARAMS.add(".+_is=\\s*");
		NON_SPLITTABLE_PARAMS.add(".+_not=\\s*");
	}
	
	/**
	 * Method to build the parameter object to be passed to solr
	 * 
	 * @param opaPath
	 *          path parameter for search engine
	 * @param queryString
	 *          the query string entered from the UI
	 * @return ModifiableSolrParams object containing the parameters
	 */
	public SolrQuery makeParams(String opaPath, String queryString, int timeout) {
		SolrQuery solrParams = new SolrQuery();
		List<NameValuePair> params = parseUriParams(queryString);
		for (NameValuePair param : params) {
			solrParams.add(param.getName(), param.getValue());
		}
		solrParams.add("opa.path", opaPath);
		if (timeout > -1) {
			solrParams.setTimeAllowed(timeout);
		}

		return solrParams;
	}

	/**
	 * Method to parse the parameters enterd in the UI
	 * 
	 * @param queryString
	 *          the query string entered from the UI
	 * @return List<NameValuePair> of parameters
	 */

	public List<NameValuePair> parseUriParams(String queryString) {
		List<NameValuePair> params = null;
		try {
			URI uri = new URI(queryString);
			params = URLEncodedUtils.parse(uri, "UTF-8");
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}

		return params;
	}

	/**
	 * Creates a map from the query string
	 * 
	 * @param queryString
	 * @return
	 */
	public Map<String, String[]> makeParamMap(String queryString) {
		HashMap<String, String[]> result = new HashMap<String, String[]>();

		// cursorMark may contain "\" character.  convert them here to percent encoded value
		// then during cursorMark processing downstream, remove.
		if (queryString.contains("\\")) {
			queryString = queryString.replaceAll("\\\\", "%5C");
		}

		for (NameValuePair pair : parseUriParams(queryString)) {
			String[] value = pair.getValue().split(",");

			for(String regex : NON_SPLITTABLE_PARAMS) {
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(pair.toString());
				if (matcher.find()) {
					value = new String[] {pair.getValue()};
					break;
				}
			}

			if (value.length > 1){
				for(String regex : NON_SPLITTABLE_VALUE_PARAMS) {
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(pair.getValue());
					if (matcher.find()) {
						value = new String[] {pair.getValue()};
						break;
					}
				}
			}
			result.put(pair.getName(), value);
		}

		return result;
	}

	/**
	 * Performs a search call to a Solr server taking timeouts into account
	 * @param solrServer
	 * @param querySolrParams
	 * @param search_timeout
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static QueryResponse doSearchWithTimeout(
			LoadBalancedHttpSolrServer solrServer, SolrParams querySolrParams,
			int search_timeout) throws InterruptedException, ExecutionException,
			TimeoutException {
		QueryResponse result = null;
		// Execute the search
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<QueryResponse> future = executor.submit(new QueryTask(solrServer,
				querySolrParams));
		try {
			result = future.get(search_timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			future.cancel(true);
			logger.error(String.format("SEARCH TIMEOUT: The following search timed out: %s", querySolrParams));
			throw e;
		}

		executor.shutdownNow();
		return result;
	}

	/**
	 * Performs a search call to a Solr gateway taking timeouts into account
	 * @param solrGateway
	 * @param queryParams
	 * @param search_timeout
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static QueryResponse doSearchWithTimeout(SolrGateway solrGateway,
			Map<String, String[]> queryParams, int search_timeout)
					throws InterruptedException, ExecutionException, TimeoutException {

		QueryResponse result = null;
		// Execute the search
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<QueryResponse> future = executor.submit(
				new QueryTask(solrGateway, queryParams));

		try {
			result = future.get(search_timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			future.cancel(true);
			logger.error(String.format("SEARCH TIMEOUT: The following search timed out: %s", queryParams));
			throw e;
		}

		executor.shutdownNow();
		return result;

	}
}
