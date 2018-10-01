package gov.nara.opa.common.services.solr.impl;

import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 * Wrapper class to represent an instance of Solr with established connection.
 */
public class SolrSearchServer {
  final HttpSolrServer solrServer;

  /*
   * long lastUsed; // last time used for a real request long lastChecked; //
   * last time checked for liveness
   */
  // "standard" servers are used by default. They normally live in the alive
  // list
  // and move to the black list when unavailable. When they become available
  // again,
  // they move back to the alive list.
  boolean standard = true;

  int failedPings = 0;

  public SolrSearchServer(HttpSolrServer solrServer) {
    this.solrServer = solrServer;

  }

  @Override
  public String toString() {
    return solrServer.getBaseURL();
  }

  public String getKey() {
    return solrServer.getBaseURL();
  }

  @Override
  public int hashCode() {
    return this.getKey().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof SolrSearchServer))
      return false;
    return this.getKey().equals(((SolrSearchServer) obj).getKey());
  }
}
