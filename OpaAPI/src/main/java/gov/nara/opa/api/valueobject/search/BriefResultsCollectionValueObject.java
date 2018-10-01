package gov.nara.opa.api.valueobject.search;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BriefResultsCollectionValueObject extends
    AbstractWebEntityValueObject {

  Double queryTime;
  List<BriefResultsValueObject> briefResults;
  Integer totalBriefResultsResults;
  Integer offset;
  Integer rows;
  Integer maxRowsForUser;

  public BriefResultsCollectionValueObject(
      List<BriefResultsValueObject> briefResults) {
    if (briefResults == null) {
      throw new OpaRuntimeException(
          "The brief results parameter cannot be null");
    }
    this.briefResults = briefResults;
    if (briefResults != null) {
      totalBriefResultsResults = getTotalBriefResults();
    }
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    if (briefResults != null && briefResults.size() > 0) {
      aspireContent.put("@queryTime", queryTime);
      aspireContent.put("@total", totalBriefResultsResults);
      aspireContent.put("@offset", offset);
      aspireContent.put("@rows", rows);
      aspireContent.put("@maxRowsForUser", maxRowsForUser);
      aspireContent.put("result", briefResults);
    }
    return aspireContent;
  }

  public Double getQueryTime() {
    return queryTime;
  }

  public void setQueryTime(Double queryTime) {
    this.queryTime = queryTime;
  }

  public Integer getTotalBriefResults() {
    return totalBriefResultsResults;
  }

  public void setTotalBriefResults(Integer totalBriefResultsResults) {
    this.totalBriefResultsResults = totalBriefResultsResults;
  }

  public List<BriefResultsValueObject> getBriefResults() {
    return briefResults;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Integer getRows() {
    return rows;
  }

  public void setRows(Integer rows) {
    this.rows = rows;
  }

  public Integer getMaxRowsForUser() {
    return maxRowsForUser;
  }

  public void setMaxRowsForUser(Integer maxRowsForUser) {
    this.maxRowsForUser = maxRowsForUser;
  }

}
