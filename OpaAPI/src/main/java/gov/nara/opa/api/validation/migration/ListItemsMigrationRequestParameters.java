package gov.nara.opa.api.validation.migration;

import java.util.LinkedHashMap;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;

public class ListItemsMigrationRequestParameters extends
    AbstractRequestParameters {

  @OpaNotNullAndNotEmpty
  @OpaPattern(regexp = "(^read$)|(^load$)", message = ErrorConstants.INVALID_MIGRATION_ACTION)
  private String action;

  private String dataType;
  
  @OpaNotNullAndNotEmpty
  private String sourceDatabaseName;
  
  private Boolean fullDetail = false;
  
  public Boolean getFullDetail() {
    return fullDetail;
  }

  public void setFullDetail(Boolean fullDetail) {
    this.fullDetail = fullDetail;
  }
  
  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public String getSourceDatabaseName() {
    return sourceDatabaseName;
  }

  public void setSourceDatabaseName(String sourceDatabaseName) {
    this.sourceDatabaseName = sourceDatabaseName;
  }

  
  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put("action", action);
    requestParams.put("sourceDatabaseName", getSourceDatabaseName());
    return requestParams;
  }

}
