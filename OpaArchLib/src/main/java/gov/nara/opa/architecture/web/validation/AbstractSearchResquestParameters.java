package gov.nara.opa.architecture.web.validation;

import java.util.Map;

public abstract class AbstractSearchResquestParameters extends
    AbstractRequestParameters {

  public static final int DEFAULT_NUMBER_OF_ROWS = 1000000;

  private String sortField;
  private String sortDirection = "ASC";
  private String sort;
  private Integer offset = 0;
  private Integer rows = DEFAULT_NUMBER_OF_ROWS;

  public AbstractSearchResquestParameters() {
    super();
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
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

  public String getSortField() {
    return sortField;
  }

  public void setSortField(String sortField) {
    this.sortField = sortField;
  }

  public String getSortDirection() {
    return sortDirection;
  }

  public void setSortDirection(String sortDirection) {
    this.sortDirection = sortDirection;
  }

  public abstract Map<String, String> getParamNamesToDbColumnsMap();
}