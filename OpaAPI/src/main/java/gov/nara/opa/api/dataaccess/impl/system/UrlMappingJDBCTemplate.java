package gov.nara.opa.api.dataaccess.impl.system;

import gov.nara.opa.api.dataaccess.system.UrlMappingDao;
import gov.nara.opa.api.valueobject.system.UrlMappingCollectionValueObject;
import gov.nara.opa.api.valueobject.system.UrlMappingValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UrlMappingJDBCTemplate extends AbstractOpaDbJDBCTemplate implements
    UrlMappingDao {

  @SuppressWarnings("unchecked")
  @Override
  public UrlMappingCollectionValueObject retrieveNaIdFromMappingTable(String recordType, int arcId) {
	  		  
	  Map<String, Object> inParamMap = new HashMap<String, Object>();
	  inParamMap.put("recordType", recordType);
	  inParamMap.put("arcId", arcId);

	  return new UrlMappingCollectionValueObject(
			  (List<UrlMappingValueObject>) StoredProcedureDataAccessUtils
			  .execute(getJdbcTemplate(), "spGetNaraId", 
					  new GenericRowMapper<UrlMappingValueObject>(new UrlMappingResultSetExtractor()), 
					  inParamMap)
			  );
  }

}
