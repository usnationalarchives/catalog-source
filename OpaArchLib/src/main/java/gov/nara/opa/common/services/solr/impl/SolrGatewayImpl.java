package gov.nara.opa.common.services.solr.impl;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.solr.SolrGateway;

import java.util.Date;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SolrGatewayImpl implements SolrGateway, InitializingBean {

  @Value("${solr.shard1.url}")
  private String solrServerShard1Url;

  @Value("${solr.replica1.url}")
  private String solrServerReplica1Url;

  @Value("${solr.shard2.url}")
  private String solrServerShard2Url;

  @Value("${solr.replica2.url}")
  private String solrServerReplica2Url;

  @Value("${serverBlacklistTimeout}")
  private String serverBlacklistTimeout;

  @Value("${serverHttpConnectionTimeout}")
  private String serverHttpConnectionTimeout;

  @Value("${serverSearchConnectionPoolSize}")
  private String serverSearchConnectionPoolSize;

  LoadBalancedHttpSolrServer solrServer;

  private OpaLogger logger = OpaLogger.getLogger(SolrGatewayImpl.class);

  @Override
  public QueryResponse solrQuery(Map<String, String[]> params, int timeout) {
    SolrQuery solrParms = new SolrQuery();
    solrParms.add(new ModifiableSolrParams(params));
    if (timeout > -1) {
      solrParms.setTimeAllowed(timeout);
    }
    return solrQuery(solrParms);
  }

  @Override
  public QueryResponse solrQuery(Map<String, String[]> params) {
    return solrQuery(params, -1);
  }

  @Override
  public QueryResponse solrQuery(SolrParams params) {
    try {
      long currentTime = (new Date()).getTime();
      logger.trace(String.format(
          "Executing query against this solr server %1$s with parameters %2$s",
          solrServerShard1Url, params.toString()));
      QueryResponse returnValue = solrServer.query(params, METHOD.POST);
      double processingTime = ((new Date()).getTime() - currentTime)
          / (double) 1000;
      logger
          .trace(String
              .format(
                  "Completed query in %1s seconds against solr server %2$s with parameters %3$s",
                  processingTime, solrServerShard1Url, params.toString()));
      return returnValue;
    } catch (SolrServerException e) {
      logger
          .error(String
              .format(
                  "Error executing query against this solr server %1$s with parameters %2$s",
                  solrServerShard1Url, params.toString()));
      throw new OpaRuntimeException(e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {

    if (solrServerShard2Url != null && !solrServerShard2Url.trim().equals("")) {
      String[][] servers = { { solrServerShard1Url, solrServerReplica1Url },
          { solrServerShard2Url, solrServerReplica2Url } };
      solrServer = new LoadBalancedHttpSolrServer(
          Integer.parseInt(serverSearchConnectionPoolSize), "select", servers);
    } else {
      String[][] servers = { { solrServerShard1Url }, { solrServerReplica1Url } };
      solrServer = new LoadBalancedHttpSolrServer(
          Integer.parseInt(serverSearchConnectionPoolSize), "select", servers);
    }

    solrServer.setAliveCheckInterval(Integer.parseInt(serverBlacklistTimeout));
    solrServer.setConnectionTimeout(Integer
        .parseInt(serverHttpConnectionTimeout));

  }

  @Override
  public SolrServer getServer() {
    return solrServer;
  }

}
