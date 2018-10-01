package gov.nara.opa.api.valueobject.search;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.SimpleOrderedMap;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class BriefResultsIntermediateValueObject extends
    AbstractWebEntityValueObject implements BriefResultsObjectConstants {
  
  private SimpleOrderedMap<Object> solrBriefResults;
  private LinkedHashMap<Object, Object> solrWebGroupResults;
  private SimpleOrderedMap<Object> solrHighlightingResults;
  private LinkedHashMap<String, LinkedHashMap<String, List<String>>> solrThesaurusMap;
  private int archivesWebCount;
  private int presidentialWebCount;
  

  public SimpleOrderedMap<Object> getSolrBriefResults() {
    return solrBriefResults;
  }

  public void setSolrBriefResults(SimpleOrderedMap<Object> solrBriefResults) {
    this.solrBriefResults = solrBriefResults;
  }

  public LinkedHashMap<Object, Object> getSolrWebGroupResults() {
    return solrWebGroupResults;
  }

  public void setSolrWebGroupResults(
      LinkedHashMap<Object, Object> solrWebGroupResults) {
    this.solrWebGroupResults = solrWebGroupResults;
  }

  public SimpleOrderedMap<Object> getSolrHighlightingResults() {
    return solrHighlightingResults;
  }

  public void setSolrHighlightingResults(
      SimpleOrderedMap<Object> solrHighlightingResults) {
    this.solrHighlightingResults = solrHighlightingResults;
  }

  public LinkedHashMap<String, LinkedHashMap<String, List<String>>> getSolrThesaurusMap() {
    return solrThesaurusMap;
  }

  public void setSolrThesaurusMap(
      LinkedHashMap<String, LinkedHashMap<String, List<String>>> solrThesaurusMap) {
    this.solrThesaurusMap = solrThesaurusMap;
  }
  
  public int getArchivesWebCount() {
    return archivesWebCount;
  }

  public void setArchivesWebCount(int archivesWebCount) {
    this.archivesWebCount = archivesWebCount;
  }

  public int getPresidentialWebCount() {
    return presidentialWebCount;
  }

  public void setPresidentialWebCount(int presidentialWebCount) {
    this.presidentialWebCount = presidentialWebCount;
  }

  @SuppressWarnings("unchecked")
  public BriefResultsIntermediateValueObject(QueryResponse qryResponse) {
    // Extract the brief results
    solrBriefResults = (SimpleOrderedMap<Object>) qryResponse.getResponse()
        .get("briefResults");

    // Extract the web results
    solrWebGroupResults = (LinkedHashMap<Object, Object>) qryResponse
        .getResponse().get("webGroupResults");

    // Extract the highlighting results
    solrHighlightingResults = (SimpleOrderedMap<Object>) qryResponse
        .getResponse().get("highlighting");

    solrThesaurusMap = (LinkedHashMap<String, LinkedHashMap<String, List<String>>>) qryResponse
        .getResponse().get("thesaurus");
  }
  

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }

}
