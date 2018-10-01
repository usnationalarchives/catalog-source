package gov.nara.opa.api.dataaccess.search;


public interface ContentDetailDao {

  // List<OpaTitle> selectOpaType(int naId) throws DataAccessException;

  boolean annotationExists(String naId, String objectId);
}
