package gov.nara.opa.api.services.impl.search;

import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 * Server container class for solr cluster connection
 */
public class OpaSolrServer extends HttpSolrServer {

  private static final long serialVersionUID = 3158937930848060179L;
  private int serverNumber;

  public OpaSolrServer(String baseURL) {
    super(baseURL);
  }

  public OpaSolrServer(String baseURL, int serverNumber) {
    super(baseURL);
    setServerNumber(serverNumber);
  }

  public void setServerNumber(int serverNumber) {
    this.serverNumber = serverNumber;
  }

  public int getServerNumber() {
    return serverNumber;
  }
}