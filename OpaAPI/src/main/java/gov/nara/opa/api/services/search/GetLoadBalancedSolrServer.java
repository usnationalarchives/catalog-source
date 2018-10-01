package gov.nara.opa.api.services.search;

import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;

public interface GetLoadBalancedSolrServer {

  LoadBalancedHttpSolrServer getServer();
}
