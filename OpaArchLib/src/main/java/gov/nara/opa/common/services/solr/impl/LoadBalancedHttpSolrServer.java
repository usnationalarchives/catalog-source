package gov.nara.opa.common.services.solr.impl;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SolrjNamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * LoadBalancedHttpSolrServer or "LoadBalanced HttpSolrServer" is a load
 * balancing wrapper around
 * {@link org.apache.solr.client.solrj.impl.HttpSolrServer}. This is useful when
 * you have multiple SolrServers and the requests need to be Load Balanced among
 * them.
 * 
 * This uses random selection solr server mechanism to find instance the request
 * is sent to, this is also shard aware , meaning is the server is down that
 * specific server is ignored for shard query and an alternative shard is set
 * from the row instead. If no shard is available for the slice of the index it
 * will omit that shard for the specific query.
 * 
 * Do <b>NOT</b> use this class for indexing in master/slave scenarios since
 * documents must be sent to the correct master; no inter-node routing is done.
 * 
 * In SolrCloud (leader/replica) scenarios, this class may be used for updates
 * since updates will be forwarded to the appropriate leader.
 * 
 * <p/>
 * It offers automatic failover when a server goes down and it detects when the
 * server comes back up.
 * <p/>
 * Load balancing is done using a simple round-robin on the list of servers.
 * <p/>
 * If a request to a server fails by an IOException due to a connection timeout
 * or read timeout then the host is taken off the list of live servers and moved
 * to a 'dead server list' and the request is resent to the next live server.
 * This process is continued till it tries all the live servers. If at least one
 * server is alive, the request succeeds, and if not it fails. <blockquote>
 * 
 * <pre>
 * SolrServer LoadBalancedHttpSolrServer = new LoadBalancedHttpSolrServer(
 *     &quot;http://host1:8080/solr/&quot;, &quot;http://host2:8080/solr&quot;,
 *     &quot;http://host2:8080/solr&quot;);
 * // or if you wish to pass the HttpClient do as follows
 * HttpClient httpClient = new HttpClient();
 * SolrServer LoadBalancedHttpSolrServer = new LoadBalancedHttpSolrServer(
 *     httpClient, &quot;http://host1:8080/solr/&quot;, &quot;http://host2:8080/solr&quot;,
 *     &quot;http://host2:8080/solr&quot;);
 * </pre>
 * 
 * </blockquote> This detects if a dead server comes alive automatically. The
 * check is done in fixed intervals in a dedicated balck/white list thread. This
 * interval can be set using {@link #setAliveCheckInterval} , the default is set
 * to one minute.
 * <p/>
 * 
 * @author apamulapati
 */
public class LoadBalancedHttpSolrServer extends SolrServer {

  Logger logger = LoggerFactory.getLogger(LoadBalancedHttpSolrServer.class);

  private static final long serialVersionUID = 6328968253574907160L;
  // all servers originally configured
  private final String[][] solrServerSets;
  private final String requestHandler;
  // keys to the maps are currently of the form "http://localhost:8983/solr"
  // which should be equivalent to CommonsHttpSolrServer.getBaseURL()
  private final Map<String, SolrSearchServer> aliveServers = new LinkedHashMap<String, SolrSearchServer>();
  // access to aliveServers should be synchronized on itself

  private final Map<String, SolrSearchServer> blackListedServers = new ConcurrentHashMap<String, SolrSearchServer>();

  // changes to aliveServers are reflected in this array, no need to synchronize
  private volatile SolrSearchServer[] aliveServerList = new SolrSearchServer[0];

  private ScheduledExecutorService aliveCheckExecutor;

  private final HttpClient httpClient;
  private final boolean clientIsInternal;
  private final Random randomGenerator = new Random();
  // empty ping query
  private static final SolrQuery solrQuery = new SolrQuery("*:*");
  private final ResponseParser parser;
  // no results expected for ping query
  static {
    solrQuery.setRows(0);
  }

  public LoadBalancedHttpSolrServer(String requestHandler,
      String[]... solrServerUrlSets) throws MalformedURLException {
    this(1, requestHandler, solrServerUrlSets);
  }

  public LoadBalancedHttpSolrServer(int maxConnectionPoolSize,
      String requestHandler, String[]... solrServerUrlSets)
      throws MalformedURLException {
    this.solrServerSets = solrServerUrlSets;
    this.requestHandler = requestHandler;
    clientIsInternal = true;
    this.parser = new BinaryResponseParser();
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set(HttpClientUtil.PROP_USE_RETRY, false);
    this.httpClient = HttpClientUtil.createClient(params);
    if (maxConnectionPoolSize > 0) {
      HttpClientUtil
          .setMaxConnectionsPerHost(httpClient, maxConnectionPoolSize);
    }
    // create pool of connections for the servers
    for (String[] serverSets : solrServerUrlSets) {
      for (int i = 0; i < serverSets.length; i++) {
        SolrSearchServer wrapper = new SolrSearchServer(
            makeServer(serverSets[i]));
        aliveServers.put(wrapper.getKey(), wrapper);
      }
    }
    updateAliveList();
  }

  public static String normalize(String server) {
    if (server.endsWith("/"))
      server = server.substring(0, server.length() - 1);
    return server;
  }

  protected HttpSolrServer makeServer(String server)
      throws MalformedURLException {
    return new HttpSolrServer(server, httpClient, parser);
  }

  /**
   * Build a unique row of shards by picking random shard from each slice.
   * 
   * @return
   */
  private String getShardsParam() {
    StringBuffer shards = new StringBuffer();
    for (int i = 0; i < this.solrServerSets.length; i++) {
      String[] solrServerSet = this.solrServerSets[i];
      int shard = randomGenerator.nextInt(solrServerSet.length);
      logger.info("alive servers :" + aliveServers + " shard checked :"
          + solrServerSet[shard]);
      // if random shard is not alive , find any shard we can use from the same
      // set
      if (!aliveServers.containsKey(solrServerSet[shard])) {
        shard = -1; // mark as bad shard
        for (int j = 0; j < solrServerSet.length; j++) {
          // check if any shard server is alive from this set
          if (aliveServers.containsKey(solrServerSet[j])) {
            shard = j;
            break;
          }
        }
      }

      if (shard != -1) {
        String shardPart = "";
        // shards do not like protocol part. we need to remove it.
        if (solrServerSet[shard].startsWith("https://")) {
          shardPart = solrServerSet[shard].substring(8,
              solrServerSet[shard].length());
        } else if (solrServerSet[shard].startsWith("http://")) {
          shardPart = solrServerSet[shard].substring(7,
              solrServerSet[shard].length());
        } else {
          shardPart = solrServerSet[shard];
        }

        shards.append(shardPart);
        if (i < this.solrServerSets.length - 1) {
          shards.append(",");
        }
      }

    }
    return shards.toString();
  }

  private void updateAliveList() {
    synchronized (aliveServers) {
      aliveServerList = aliveServers.values().toArray(
          new SolrSearchServer[aliveServers.size()]);
    }
  }

  private SolrSearchServer removeFromAlive(String key) {
    synchronized (aliveServers) {
      SolrSearchServer wrapper = aliveServers.remove(key);
      if (wrapper != null)
        updateAliveList();
      return wrapper;
    }
  }

  private void addToAlive(SolrSearchServer wrapper) {
    synchronized (aliveServers) {
      aliveServers.put(wrapper.getKey(), wrapper);
      updateAliveList();
    }
  }

  public void addSolrServer(String server) throws MalformedURLException {
    HttpSolrServer solrServer = makeServer(server);
    addToAlive(new SolrSearchServer(solrServer));
  }

  public String removeSolrServer(String server) {
    try {
      server = new URL(server).toExternalForm();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    if (server.endsWith("/")) {
      server = server.substring(0, server.length() - 1);
    }

    // there is a small race condition here - if the server is in the process of
    // being moved between
    // lists, we could fail to remove it.
    removeFromAlive(server);
    blackListedServers.remove(server);
    return null;
  }

  public void setConnectionTimeout(int timeout) {
    HttpClientUtil.setConnectionTimeout(httpClient, timeout);
  }

  /**
   * set soTimeout (read timeout) on the underlying HttpConnectionManager. This
   * is desirable for queries, but probably not for indexing.
   */
  public void setSoTimeout(int timeout) {
    HttpClientUtil.setSoTimeout(httpClient, timeout);
  }

  @Override
  public void shutdown() {
    if (aliveCheckExecutor != null) {
      aliveCheckExecutor.shutdownNow();
    }
    if (clientIsInternal) {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tries to query a live server. A SolrServerException is thrown if all
   * servers are dead. If the request failed due to IOException then the live
   * server is moved to dead pool and the request is retried on another live
   * server. After live servers are exhausted, any servers previously marked as
   * dead will be tried before failing the request. This is the specialization
   * of Out of the box SolrServer request(..) implementation to override the
   * behavior and do load balancing.
   * 
   * @param request
   *          the SolrRequest.
   * 
   * @return response
   * 
   * @throws IOException
   *           If there is a low-level I/O error.
   */
  @Override
  public NamedList<Object> request(final SolrRequest request)
      throws SolrServerException, IOException {
	String query = request.getParams().toString();
	  
    Exception ex = null;
    SolrSearchServer[] serverList = aliveServerList;

    int maxTries = serverList.length;
    Map<String, SolrSearchServer> justFailed = null;

    for (int attempts = 0; attempts < maxTries; attempts++) {
      // randomly choose a primary server for the set by picking uniformly
      // distributed int value between 0 (inclusive)
      // and the aliveServerList size value (exclusive)
      int serverPicked = randomGenerator.nextInt(serverList.length);
      SolrSearchServer wrapper = serverList[serverPicked];

      // wrapper.lastUsed = System.currentTimeMillis();
      String shards = getShardsParam();
      System.out.println("Server picked :" + serverPicked + ") " + wrapper
          + " shards : " + shards);
      logger.info("Server picked :" + serverPicked + ") " + wrapper
          + " shards : " + shards);
      ((ModifiableSolrParams) request.getParams()).set("shards", shards);

      ((ModifiableSolrParams) request.getParams()).set("shards.qt",
          requestHandler);
      ((ModifiableSolrParams) request.getParams()).set("qt", requestHandler);
      ((ModifiableSolrParams) request.getParams()).set("requestHandler", "/"
          + requestHandler);

      try {
        return wrapper.solrServer.request(request);
      } catch (SolrException e) {
        // Server is alive but the request was malformed or invalid
        logger.error(String.format("Solr Request Error: SolrException. Query: %1$s", query), e);
        throw e;
      } catch (SolrServerException e) {
    	
        if (e.getRootCause() instanceof IOException) {
          ex = e;

          logger.error(String.format("Server failed to provide response to request, moved server %1$s to blacklisted servers. Query: %2$s", wrapper.getKey(), query), e);
          
          // server failed to provide response to request , move this server to
          // blacklisted servers.
          moveAliveToBlackList(wrapper);
          if (justFailed == null)
            justFailed = new HashMap<String, SolrSearchServer>();
          justFailed.put(wrapper.getKey(), wrapper);
        } else {
          logger.error(String.format("Solr Request Error: SolrServerException. Query: %1$s", query), e);
          throw e;
        }
      } catch (Exception e) {
    	logger.error(String.format("Solr Request Error: Unknown exception. Query: %1$s", query), e);
        throw new SolrServerException(e);
      }
    }

    // try other standard servers that we didn't try just now
    for (SolrSearchServer wrapper : blackListedServers.values()) {
      if (wrapper.standard == false || justFailed != null
          && justFailed.containsKey(wrapper.getKey()))
        continue;
      try {
        NamedList<Object> rsp = wrapper.solrServer.request(request);
        // remove from black list *before* adding to alive to avoid a race that
        // could lose a server
        blackListedServers.remove(wrapper.getKey());
        addToAlive(wrapper);
        return rsp;
      } catch (SolrException e) {
        // Server is alive but the request was malformed or invalid
    	logger.error(String.format("Solr Request Error: SolrException. Query: %1$s", query), e);
        throw e;
      } catch (SolrServerException e) {
    	logger.error(String.format("Solr Request Error: SolrServerException. Query: %1$s", query), e);
        if (e.getRootCause() instanceof IOException) {
          ex = e;
          // still dead
        } else {
          throw e;
        }
      } catch (Exception e) {
    	logger.error(String.format("Solr Request Error: Unknown exception. Query: %1$s", query), e);
        throw new SolrServerException(e);
      }
    }

    if (ex == null) {
      throw new SolrServerException(
          "No live SolrServers available to handle this request");
    } else {
      throw new SolrServerException(
          "No live SolrServers available to handle this request", ex);
    }
  }

  /**
   * Takes up one dead server and check for aliveness. The check is done in a
   * roundrobin. Each server is checked for aliveness once in 'x' millis where x
   * is decided by the setAliveCheckinterval() or it is defaulted to 1 minute
   * 
   * @param blackListServer
   *          a server in the dead pool
   */
  private void checkABlackListServer(SolrSearchServer blackListServer) {
    // long currTime = System.currentTimeMillis();
    try {
      // blackListServer.lastChecked = currTime;
      QueryResponse resp = blackListServer.solrServer.query(solrQuery);
      if (resp.getStatus() == 0) {
        // server has come back up.
        // make sure to remove from blacklist before adding to alive to avoid a
        // race condition
        // where another thread could mark it down, move it back to blacklist,
        // and then we delete
        // from blacklist and lose it forever.
        SolrSearchServer wrapper = blackListedServers.remove(blackListServer
            .getKey());
        if (wrapper != null) {
          wrapper.failedPings = 0;
          if (wrapper.standard) {
            addToAlive(wrapper);
          }
        } else {
          // something else already moved the server from baclklist to alive
        }
      }
    } catch (Exception e) {
      // Expected. The server is still down.
      blackListServer.failedPings++;

      // If the server doesn't belong in the standard set belonging to this load
      // balancer
      // then simply drop it after a certain number of failed pings.
      if (!blackListServer.standard
          && blackListServer.failedPings >= NONSTANDARD_PING_LIMIT) {
        blackListedServers.remove(blackListServer.getKey());
      }
    }
  }

  private void moveAliveToBlackList(SolrSearchServer wrapper) {
    wrapper = removeFromAlive(wrapper.getKey());
    if (wrapper == null)
      return; // another thread already detected the failure and removed it
    blackListedServers.put(wrapper.getKey(), wrapper);
    startAliveCheckExecutor();
  }

  // check black list servers health at each of this interval
  private int interval = CHECK_INTERVAL;

  /**
   * LoadBalancedHttpSolrServer keeps pinging the dead servers at fixed interval
   * to find if it is alive. Use this to set that interval
   * 
   * @param interval
   *          time in milliseconds
   */
  public void setAliveCheckInterval(int interval) {
    if (interval <= 0) {
      throw new IllegalArgumentException("Alive check interval must be "
          + "positive, specified value = " + interval);
    }
    this.interval = interval;
  }

  private void startAliveCheckExecutor() {
    // double-checked locking, but it's OK because we don't *do* anything with
    // aliveCheckExecutor
    // if it's not null.
    if (aliveCheckExecutor == null) {
      synchronized (this) {
        if (aliveCheckExecutor == null) {
          aliveCheckExecutor = Executors
              .newSingleThreadScheduledExecutor(new SolrjNamedThreadFactory(
                  "aliveCheckExecutor"));
          aliveCheckExecutor
              .scheduleAtFixedRate(
                  getAliveCheckRunner(new WeakReference<LoadBalancedHttpSolrServer>(
                      this)), this.interval, this.interval,
                  TimeUnit.MILLISECONDS);
        }
      }
    }
  }

  /**
   * BlackList thread process
   * 
   * @param lbRef
   * @return
   */
  private static Runnable getAliveCheckRunner(
      final WeakReference<LoadBalancedHttpSolrServer> lbRef) {
    return new Runnable() {
      @Override
      public void run() {
        LoadBalancedHttpSolrServer lb = lbRef.get();
        if (lb != null && lb.blackListedServers != null) {
          for (SolrSearchServer blacklistServer : lb.blackListedServers
              .values()) {
            lb.checkABlackListServer(blacklistServer);
          }
        }
      }
    };
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      if (this.aliveCheckExecutor != null)
        this.aliveCheckExecutor.shutdownNow();
    } finally {
      super.finalize();
    }
  }

  // defaults
  private static final int CHECK_INTERVAL = 60 * 1000; // 1 minute between
                                                       // checks
  private static final int NONSTANDARD_PING_LIMIT = 5; // number of times we'll
                                                       // ping dead servers not
                                                       // in the server list

}
