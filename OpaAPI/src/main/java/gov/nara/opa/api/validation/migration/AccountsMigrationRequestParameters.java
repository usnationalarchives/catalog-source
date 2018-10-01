package gov.nara.opa.api.validation.migration;

import java.util.LinkedHashMap;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;

/**
 * Request parameters for accounts migration
 *
 */
public class AccountsMigrationRequestParameters extends
    AbstractRequestParameters {

  @OpaNotNullAndNotEmpty
  @OpaPattern(regexp = "(^read$)|(^load$)", message = ErrorConstants.INVALID_MIGRATION_ACTION)
  String action;

  String dataType;

  @OpaNotNullAndNotEmpty
  String dataFile;
  
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

  public String getDataFile() {
    return dataFile;
  }

  public void setDataFile(String dataFile) {
    this.dataFile = dataFile;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put("action", action);
    requestParams.put("dataType", dataType);
    requestParams.put("dataFile", dataFile);
    return requestParams;
  }


}
