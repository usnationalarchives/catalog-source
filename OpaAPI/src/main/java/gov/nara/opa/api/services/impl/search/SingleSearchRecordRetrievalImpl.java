package gov.nara.opa.api.services.impl.search;

import gov.nara.opa.api.services.search.SingleSearchRecordRetrieval;
import gov.nara.opa.common.services.solr.SolrGateway;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObjectHelper;

import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SingleSearchRecordRetrievalImpl implements
    SingleSearchRecordRetrieval {

  @Autowired
  SolrGateway solrGateway;

  @Autowired
  SearchRecordValueObjectHelper solrRecordValueObjectHelper;

  @Override
  public SearchRecordValueObject getSearchRecord(
      Map<String, String[]> queryParameters) throws SolrServerException {
    queryParameters.put("rows", new String[] { "1" });
    QueryResponse queryResponse = solrGateway.solrQuery(queryParameters);
    if (queryResponse.getResults().size() > 0) {
      SolrDocument solrDocument = queryResponse.getResults().get(0);
      if (solrDocument.get("naId") == null && solrDocument.get("parentDescriptionNaId") == null) {
        return null;
      }
      return solrRecordValueObjectHelper.createSolrRecord(solrDocument);
    }
    return null;
  }
}
