package gov.nara.opa.api.validation.search;

import gov.nara.opa.api.utils.SearchUtils;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import java.util.LinkedHashMap;

public class BriefResultsRequestParameters extends AbstractRequestParameters {

  private String tabType = "all";
  
  @OpaNotNullAndNotEmpty
  private Integer offset = 0;
  
  @OpaNotNullAndNotEmpty
  private Integer rows = 20;
  private String ancestorNaIds = "";
  private String searchType = "";
  private boolean highlight = true;
  private String accountType = "";

  @OpaNotNullAndNotEmpty
  private String action;

  @OpaNotNullAndNotEmpty
  private String q;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getQ() {
    return q;
  }

  public void setQ(String q) {
    this.q = q;
  }

  public String getTabType() {
    return tabType;
  }

  public void setTabType(String tabType) {
    this.tabType = tabType;
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

  public String getAncestorNaIds() {
    return ancestorNaIds;
  }

  public void setAncestorNaIds(String ancestorNaIds) {
    this.ancestorNaIds = ancestorNaIds;
  }

  public String getSearchType() {
    return searchType;
  }

  public void setSearchType(String searchType) {
    this.searchType = searchType;
  }

  public boolean getHighlight() {
    return highlight;
  }

  public void setHighlight(boolean highlight) {
    this.highlight = highlight;
  }

  public String getAccountType() {
    return accountType;
  }

  public void setAccountType(String accountType) {
    this.accountType = accountType;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put("query", q);
    return requestParams;
  }
  
  @Override
  public boolean isInWhiteList(String parameterName) {
    return SearchUtils.isInWhiteList(parameterName);
  }

}
