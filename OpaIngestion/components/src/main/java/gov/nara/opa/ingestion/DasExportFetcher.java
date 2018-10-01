/**
 * Copyright Search Technologies 2013
 */

package gov.nara.opa.ingestion;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.StageImpl;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;

/**
 * Fetcher for DasExport connector
 * <p/>
 * This component is responsible of opening input streams to the content of each source item, so text extraction and other metadata manipulation can take place. 
 * <p/>
 * The fetcher component will be called for each source item (not containers by default) by multiple threads concurrently.
 * <p/>
 * <b>Not all connectors need a fetcher component. Aspire already has a generic Fetcher component which opens streams to URLs (including URLs to resources on the file system).</b> 
 * @author jmendez
 */
public class DasExportFetcher extends StageImpl {
  /**
   * This is the entry point of the fetcher component. Make sure this method is thread safe!!!
   * <p/>
   * Open an input stream for the item content here. At this point, you can assume containers were already filtered.
   */
  @Override
  public void process(Job j) throws AspireException {
    /*
     * Open an stream to the job's document content.
     * 
     *  1) Get the AspireObject from job
     *  2) Fetch any necessary parameters from the AspireObject to open the content stream (i.e. URL, credentials, path to a file in disk, etc).
     *  3) Open the stream
     *  4) Store it as a job variable (and don't forget to register a closeable object on job).
     */
  }

  @Override
  public void initialize(Element config) throws AspireException {
    // TODO Do any initialization from the application configuration.
  }

  @Override
  public void close() {
    // TODO Release any allocated resources.
  }
}