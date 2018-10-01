package gov.nara.opa.api.valueobject.search;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebResultsCollectionValueObject extends
    AbstractWebEntityValueObject {

  List<WebResultsValueObject> webResults;
  Integer totalWebResultsResults;
  Integer offset;
  Integer rows;

  public WebResultsCollectionValueObject(List<WebResultsValueObject> webResults) {
    if (webResults == null) {
      throw new OpaRuntimeException("The web results parameter cannot be null");
    }
    this.webResults = webResults;
    if (webResults != null) {
      totalWebResultsResults = getTotalWebResults();
    }
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    if (webResults != null && webResults.size() > 0) {
      aspireContent.put("@total", totalWebResultsResults);
      aspireContent.put("@offset", offset);
      aspireContent.put("@rows", rows);
      aspireContent.put("result", webResults);
    }
    return aspireContent;
  }

  public void setTotalWebResults(Integer totalWebResultsResults) {
    this.totalWebResultsResults = totalWebResultsResults;
  }

  public Integer getTotalWebResults() {
    return totalWebResultsResults;
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

  public List<WebResultsValueObject> getWebResults() {
    return webResults;
  }

}
