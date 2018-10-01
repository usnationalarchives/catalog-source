package gov.nara.opa.common.valueobject.search;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class SolrQueryResponseValueObjectHelper {
  public static SolrQueryResponseValueObject create(QueryResponse queryResponse) {
    SolrQueryResponseValueObject returnValue = new SolrQueryResponseValueObject();
    returnValue.setQueryTime(queryResponse.getQTime());
    SolrDocumentList resultsList = queryResponse.getResults();
    if (resultsList == null) {
      returnValue.setTotalResults(0);
    } else {
      returnValue.setTotalResults(resultsList.getNumFound());
      returnValue.setNextCursorMark(queryResponse.getNextCursorMark());
    }
    return returnValue;
  }
}
