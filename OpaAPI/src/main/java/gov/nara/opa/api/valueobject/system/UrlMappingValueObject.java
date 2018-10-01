package gov.nara.opa.api.valueobject.system;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UrlMappingValueObject extends AbstractWebEntityValueObject implements 
		UrlMappingValueObjectConstants {

	private String recordType;
	private Integer arcId;
	private Integer naId = 0;

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public Integer getArcId() {
		return arcId;
	}

	public void setArcId(int arcId) {
		this.arcId = arcId;
	}

	public Integer getNaraId() {
		return naId;
	}

	public void setNaraId(int naId) {
		this.naId = naId;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new HashMap<String, Object>();

		databaseContent.put(ARC_ID_DB, getArcId());
		databaseContent.put(RECORD_TYPE_DB, getRecordType());
		databaseContent.put(NARA_ID_DB, getNaraId());

		return databaseContent;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

		aspireContent.put(ARC_ID_ASP, getArcId());
		aspireContent.put(RECORD_TYPE_ASP, getRecordType());
		aspireContent.put(NARA_ID_ASP, getNaraId());

		return aspireContent;
	}

}