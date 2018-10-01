package gov.nara.opa.api.services.impl.system;

import gov.nara.opa.api.dataaccess.system.UrlMappingDao;
import gov.nara.opa.api.services.system.UrlMappingService;
import gov.nara.opa.api.validation.system.UrlMappingRequestParameters;
import gov.nara.opa.api.valueobject.system.UrlMappingCollectionValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class UrlMappingServiceImpl implements UrlMappingService {

  @Autowired
  private UrlMappingDao urlMappingDao;

  @Override
  public UrlMappingCollectionValueObject getUrlMapping(
      UrlMappingRequestParameters requestParameters) {

	  String recordType = requestParameters.getRecordType();
	  int arcId = requestParameters.getArcId();

	  return urlMappingDao.retrieveNaIdFromMappingTable(recordType, arcId);
  }

}
