package gov.nara.opa.api.validation.system;

import gov.nara.opa.api.valueobject.system.UrlMappingValueObjectConstants;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import java.util.LinkedHashMap;

public class UrlMappingRequestParameters extends AbstractRequestParameters
    implements UrlMappingValueObjectConstants {

  @OpaNotNullAndNotEmpty
  String recordType;

  @OpaNotNullAndNotEmpty
  int arcId;

  public String getRecordType() {
    return recordType;
  }

  public void setRecordType(String recordType) {
    this.recordType = recordType;
  }

  public int getArcId() {
    return arcId;
  }

  public void setArcId(int arcId) {
    this.arcId = arcId;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(RECORD_TYPE_ASP, getRecordType());
    requestParams.put(ARC_ID_ASP, getArcId());
    return requestParams;
  }

}
