package gov.nara.opa.api.services.impl.search;

import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class GetLoadBalancedSolrServerImpl implements
    GetLoadBalancedSolrServer, InitializingBean {

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

  @Override
  public LoadBalancedHttpSolrServer getServer() {
    try {
      return solrServer;
    } catch (OpaRuntimeException e) { // SolrServerException
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

}
