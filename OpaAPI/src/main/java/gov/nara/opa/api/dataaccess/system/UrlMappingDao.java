package gov.nara.opa.api.dataaccess.system;

import gov.nara.opa.api.valueobject.system.UrlMappingCollectionValueObject;

public interface UrlMappingDao {
  UrlMappingCollectionValueObject retrieveNaIdFromMappingTable(String recordType, int arcId);
}