package gov.nara.opa.api.validation.annotation.summary;

import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.NumericConstants;

import java.util.LinkedHashMap;

public class SummaryRequestParameters extends AbstractRequestParameters {

	@OpaNotNullAndNotEmpty
	@OpaSize(errorCode = ArchitectureErrorCodeConstants.INVALID_VALUE, max = NumericConstants.NA_ID_LENGTH)
	private String naId;

	@OpaSize(errorCode = ArchitectureErrorCodeConstants.INVALID_VALUE, max = NumericConstants.OBJECT_ID_LENGTH)
	private String objectId;

	@OpaNotNullAndNotEmpty
	private String userName = "";

	@OpaNotNullAndNotEmpty
	@OpaSize(errorCode = ArchitectureErrorCodeConstants.INVALID_VALUE)
	private int rows = 25;

	@OpaNotNullAndNotEmpty
	@OpaSize(errorCode = ArchitectureErrorCodeConstants.INVALID_VALUE)
	private int offset = 0;

	@OpaNotNullAndNotEmpty
	private String include;

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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getInclude() {
		return include;
	}

	public void setInclude(String include) {
		this.include = include;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();

		requestParams.put("naId", naId);
		if (!StringUtils.isNullOrEmtpy(objectId)) {
			requestParams.put("objectId", objectId);
		}
		requestParams.put("userNae", userName);
		requestParams.put("rows", rows);
		requestParams.put("offset", offset);
		requestParams.put("include", include);
		return requestParams;
	}

}
