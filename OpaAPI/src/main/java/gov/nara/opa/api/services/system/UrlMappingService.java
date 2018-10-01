package gov.nara.opa.api.services.system;

import gov.nara.opa.api.validation.system.UrlMappingRequestParameters;
import gov.nara.opa.api.valueobject.system.UrlMappingCollectionValueObject;

public interface UrlMappingService {

  public UrlMappingCollectionValueObject getUrlMapping(
      UrlMappingRequestParameters requestParameters);
}
