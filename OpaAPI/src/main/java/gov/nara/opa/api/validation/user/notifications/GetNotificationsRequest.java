package gov.nara.opa.api.validation.user.notifications;

import java.util.LinkedHashMap;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaGreaterThanValue;

public class GetNotificationsRequest extends AbstractRequestParameters {

	@OpaGreaterThanValue(min = -1)
	private int offset;
	
	@OpaGreaterThanValue(min = 0)
	private int rows;
	
	private boolean showAll;
	
	
	public int getOffset() {
		return offset;
	}


	public void setOffset(int offset) {
		this.offset = offset;
	}


	public int getRows() {
		return rows;
	}


	public void setRows(int rows) {
		this.rows = rows;
	}


	public boolean isShowAll() {
		return showAll;
	}


	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}


	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		// TODO Auto-generated method stub
		return null;
	}

}
