package gov.nara.opa.api.validation.export;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import java.util.LinkedHashMap;

public class GetAccountExportStatusRequestParameters extends
    AbstractRequestParameters {

  @OpaNotNullAndNotEmpty
  private Integer bulkExportId;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put("exportId", getBulkExportId());
    return requestParams;
  }

  public Integer getBulkExportId() {
    return bulkExportId;
  }

  public void setBulkExportId(Integer bulkExportId) {
    this.bulkExportId = bulkExportId;
  }

}
