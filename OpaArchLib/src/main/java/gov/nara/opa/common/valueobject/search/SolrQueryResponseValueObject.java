package gov.nara.opa.common.valueobject.search;

public class SolrQueryResponseValueObject {

  private int queryTime;
  private long totalResults;
  private String nextCursorMark;

  public int getQueryTime() {
    return queryTime;
  }

  public void setQueryTime(int queryTime) {
    this.queryTime = queryTime;
  }

  public long getTotalResults() {
    return totalResults;
  }

  public void setTotalResults(long totalResults) {
    this.totalResults = totalResults;
  }

  public String getNextCursorMark() {
    return nextCursorMark;
  }

  public void setNextCursorMark(String nextCursorMark) {
    this.nextCursorMark = nextCursorMark;
  }
  
  
}
