package gov.nara.opa.api.valueobject.administrator;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AccountMaintenanceValueObject extends AbstractWebEntityValueObject
	implements AccountMaintenanceValueObjectConstants {

	private int accountId;
	private String email;
	private int remaining;
	private String actionType;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getRemaining() {
		return remaining;
	}

	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new HashMap<String, Object>();
		databaseContent.put(ACCOUNT_ID_DB, getAccountId());
		databaseContent.put(EMAIL_ADDRESS_DB, getEmail());
		databaseContent.put(REMAINING_DB, getRemaining());
		databaseContent.put(ACTION_TYPE_DB, getActionType());
		return databaseContent;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		return null;
	}

}
