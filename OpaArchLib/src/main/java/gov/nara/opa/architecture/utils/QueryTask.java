package gov.nara.opa.architecture.utils;

import gov.nara.opa.common.services.solr.SolrGateway;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

class QueryTask implements Callable<QueryResponse> {

	LoadBalancedHttpSolrServer solrServer;
	SolrGateway solrGateway;
	SolrParams querySolrParams;
	Map<String,String[]> queryParams;

	public QueryTask(LoadBalancedHttpSolrServer solrServer,
			SolrParams querySolrParams) {
		this.solrServer = solrServer;
		this.querySolrParams = querySolrParams;
	}

	public QueryTask(SolrGateway solrGateway,
			SolrParams querySolrParams) {
		this.solrGateway = solrGateway;
		this.querySolrParams = querySolrParams;
	}

	public QueryTask(SolrGateway solrGateway,
			Map<String, String[]> queryParams) {
		this.solrGateway = solrGateway;
		this.queryParams = queryParams;
	}


	@Override
	public QueryResponse call() throws Exception {
		if(solrServer != null) {
			return solrServer.query(querySolrParams);
		} else if (solrGateway != null) {
			if(querySolrParams != null) {
				return solrGateway.solrQuery(querySolrParams);
			} else if(queryParams != null) {
				return solrGateway.solrQuery(queryParams);
			}
		}
		return null;
	}

}
