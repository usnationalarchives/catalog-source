package gov.nara.opa.api.services.search;

import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;

public interface SingleSearchRecordRetrieval {

  SearchRecordValueObject getSearchRecord(Map<String, String[]> queryParameters) throws SolrServerException;
}
