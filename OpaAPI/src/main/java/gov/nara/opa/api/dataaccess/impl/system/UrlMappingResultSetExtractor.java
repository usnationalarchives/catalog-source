package gov.nara.opa.api.dataaccess.impl.system;

import gov.nara.opa.api.valueobject.system.UrlMappingValueObject;
import gov.nara.opa.api.valueobject.system.UrlMappingValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class UrlMappingResultSetExtractor implements 
	ResultSetExtractor<UrlMappingValueObject>, UrlMappingValueObjectConstants {

  @Override
  public UrlMappingValueObject extractData(ResultSet rs) 
		  throws SQLException {
	  
	  UrlMappingValueObject urlMapping = new UrlMappingValueObject();
	  urlMapping.setRecordType(rs.getString(RECORD_TYPE_DB));
	  urlMapping.setArcId(rs.getInt(ARC_ID_DB));
	  urlMapping.setNaraId(rs.getInt(NARA_ID_DB));

	  return urlMapping;
  }

}
