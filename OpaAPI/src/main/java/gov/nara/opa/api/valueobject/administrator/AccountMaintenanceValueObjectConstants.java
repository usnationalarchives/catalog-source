package gov.nara.opa.api.valueobject.administrator;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface AccountMaintenanceValueObjectConstants 
	extends CommonValueObjectConstants {

	public static final String ACCOUNT_ID_DB = "account_id";
	public static final String EMAIL_ADDRESS_DB = "email_address";
	public static final String REMAINING_DB = "remaining";
	public static final String ACTION_TYPE_DB = "action_type";

	public static final String DEACTIVATE = "DEACTIVATE";
	public static final String SECOND_WARNING = "SECOND_WARNING";
	public static final String FIRST_WARNING = "FIRST_WARNING";

}
