package gov.nara.opa.api.validation.export;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import java.util.LinkedHashMap;

public class DeleteAccountExportRequestParameters extends
    AbstractRequestParameters {

  @OpaNotNullAndNotEmpty
  Integer exportId;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put("exportId", getExportId());
    return requestParams;
  }

  public Integer getExportId() {
    return exportId;
  }

  public void setExportId(Integer exportId) {
    this.exportId = exportId;
  }

}
