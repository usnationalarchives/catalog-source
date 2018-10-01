package gov.nara.opa.api.services.search;

import gov.nara.opa.api.validation.search.ContentDetailRequestParameters;
import gov.nara.opa.api.validation.search.PagedContentDetailRequestParameters;
import gov.nara.opa.api.valueobject.search.ContentDetailValueObject;

import java.io.IOException;

public interface ContentDetailService {

  /**
   * Method to retrieve the content detail: Descriptions.xml, Objects.xml and
   * Annotations for an naId
   * 
   * @param naId
   *          NAID
   * @return ContentDetailValueObject
   * @throws IOException
   */
  public ContentDetailValueObject getContentDetail(
      ContentDetailRequestParameters requestParameters, String opaPath,
      String query);
  
  public ContentDetailValueObject getContentDetail(
      PagedContentDetailRequestParameters requestParameters, String opaPath,
      String query);
  
}
