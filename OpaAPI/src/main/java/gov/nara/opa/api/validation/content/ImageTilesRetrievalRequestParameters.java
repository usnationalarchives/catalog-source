package gov.nara.opa.api.validation.content;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import java.util.LinkedHashMap;

public class ImageTilesRetrievalRequestParameters extends
    AbstractRequestParameters {

  @OpaNotNullAndNotEmpty
  private String naId;
  
  @OpaNotNullAndNotEmpty
  private String objectId;
  
  
  public String getNaId() {
    return naId;
  }



  public void setNaId(String naId) {
    this.naId = naId;
  }



  public String getObjectId() {
    return objectId;
  }



  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }



  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

    result.put("naId", naId);
    result.put("objectId", objectId);
    
    return result;
  }

}
