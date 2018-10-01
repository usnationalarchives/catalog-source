package gov.nara.opa.api.valueobject.system;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class UrlMappingCollectionValueObject extends AbstractWebEntityValueObject 
		implements UrlMappingValueObjectConstants {
	
	private List<UrlMappingValueObject> urlMappings;
	private int totalUrlMappings;
	private String entityName;
	
	public UrlMappingCollectionValueObject(List<UrlMappingValueObject> urlMappings) {
	    if (urlMappings == null) {
	      throw new OpaRuntimeException("The url mappings parameter cannot be null");
	    }
	    this.urlMappings = urlMappings;
	    if (urlMappings != null) {
	    	totalUrlMappings = urlMappings.size();
	    	entityName = "naIds";
	    }
	  }

	@Override
	public Map<String, Object> getDatabaseContent() {
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
	    if (urlMappings != null && urlMappings.size() > 0) {
	      aspireContent.put("@total", totalUrlMappings);
	      aspireContent.put("naId", urlMappings);
	    }
	    return aspireContent;
	}
	
	public Integer getTotalUrlMappings() {
	    return totalUrlMappings;
	}
	
	public List<UrlMappingValueObject> getUrlMappings() {
	    return urlMappings;
	}
	
	public String getEntityName() {
		return entityName;
	}
}
