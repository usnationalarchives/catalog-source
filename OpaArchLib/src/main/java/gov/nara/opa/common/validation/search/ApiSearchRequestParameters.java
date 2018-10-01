package gov.nara.opa.common.validation.search;

import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.services.SingletonServices;

public class ApiSearchRequestParameters extends AbstractRequestParameters {

  @OpaNotNullAndNotEmpty
  private Integer rows = 10;

  @OpaNotNullAndNotEmpty
  private Integer offset = 0;

  private Map<String, String[]> queryParameters;

  private String sort;

  private Boolean recordLine;

  private String queryString;
  
  private String accountType;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put("action", "search");
    if (queryString != null && !queryString.equals("")) {
      String[] splitQueryString = queryString.split("&");
      for (int i = 0; i < splitQueryString.length; i++) {
        String queryParam = splitQueryString[i].trim();
        String queryParamName = "";
        String queryParamValue = "";
        
        String[] splitQueryParam = queryParam.split("=");
        if (splitQueryParam.length > 0) {
          queryParamName = splitQueryParam[0];
        }
        if (splitQueryParam.length > 1) {
        	//could have = in the value, so add the rest
          int equalsLocation = queryParam.indexOf('=');
          queryParamValue = queryParam.substring(equalsLocation+1, queryParam.length());
        }
        if (!queryParamName.equals("")) {
          requestParams.put(queryParamName, queryParamValue);
        }
      }
    }

    return requestParams;
  }

  public Integer getRows() {
    return rows;
  }

  public void setRows(Integer rows) {
    this.rows = rows;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Map<String, String[]> getQueryParameters() {
    return queryParameters;
  }

  public void setQueryParameters(Map<String, String[]> queryParameters) {
    this.queryParameters = queryParameters;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public Boolean getRecordLine() {
    return recordLine;
  }

  public void setRecordLine(Boolean recordLine) {
    this.recordLine = recordLine;
  }

  @Override
  public boolean isInWhiteList(String parameterName) {

    boolean inWhiteList = SingletonServices.SOLR_FIELDS_WHITE_LIST
        .contains(parameterName);
    if (inWhiteList) {
      return true;
    }
    // TODO: add "ends with" list
    // to avoid having to create a new list for testing, we'll use STARTS_WITH
    for(Object suffixObj : SingletonServices.SOLR_FIELDS_STARTS_WITH_LIST.toArray()) {
      String suffix = suffixObj.toString();
      if(parameterName.endsWith(suffix)) {
        return SingletonServices.DAS_WHITE_LIST.contains(parameterName.replace(suffix,""));
      }
    }
    return SingletonServices.DAS_WHITE_LIST.contains(parameterName);
  }

  public String getQueryString() {
    return queryString;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

public String getAccountType() {
	return accountType;
}

public void setAccountType(String accountType) {
	this.accountType = accountType;
}
}
