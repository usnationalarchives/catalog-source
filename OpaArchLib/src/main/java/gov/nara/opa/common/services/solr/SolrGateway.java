package gov.nara.opa.common.services.solr;

import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

public interface SolrGateway {

  QueryResponse solrQuery(SolrParams params);

  QueryResponse solrQuery(Map<String, String[]> params);

  SolrServer getServer();

  QueryResponse solrQuery(Map<String, String[]> params, int timeout);
}
