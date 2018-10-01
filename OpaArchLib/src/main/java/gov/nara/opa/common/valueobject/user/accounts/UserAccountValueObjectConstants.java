package gov.nara.opa.common.valueobject.user.accounts;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface UserAccountValueObjectConstants extends
    CommonValueObjectConstants {

	// Columns from the ACCOUNTS table
	public static final String ACCOUNT_ID_DB = "ACCOUNT_ID";
	public static final String ACCOUNT_TYPE_DB = "ACCOUNT_TYPE";
	public static final String ACCOUNT_RIGHTS_DB = "ACCOUNT_RIGHTS";
	public static final String USER_NAME_DB = "USER_NAME";
	public static final String FULL_NAME_DB = "FULL_NAME";
	public static final String DISPLAY_NAME_FLAG_DB = "DISPLAY_NAME_FLAG";
	public static final String EMAIL_ADDRESS_DB = "EMAIL_ADDRESS";
	public static final String ADDITIONAL_EMAIL_ADDRESS_DB = "ADDITIONAL_EMAIL_ADDRESS";
	public static final String ADDITIONAL_EMAIL_ADDRESS_TS_DB = "ADDITIONAL_EMAIL_ADDRESS_TS";
	public static final String IS_NARA_STAFF_DB = "IS_NARA_STAFF";
	public static final String P_WORD_DB = "P_WORD";
	public static final String LAST_NOTIFICATION_ID_DB = "LAST_NOTIFICATION_ID";
	public static final String ACCOUNT_STATUS_DB = "ACCOUNT_STATUS";
	public static final String ACCOUNT_REASON_FLAG_DB = "ACCOUNT_REASON_FLAG";
	public static final String ACCOUNT_NOTE_FLAG_DB = "ACCOUNT_NOTE_FLAG";
	public static final String ACCOUNT_CREATED_TS_DB = "ACCOUNT_CREATED_TS";
	public static final String P_WORD_CHANGE_ID_DB = "P_WORD_CHANGE_ID";
	public static final String P_WORD_CHANGE_TS_DB = "P_WORD_CHANGE_TS";
	public static final String VERIFICATION_GUID_DB = "VERIFICATION_GUID";
	public static final String VERIFICATION_TS_DB = "VERIFICATION_TS";
	public static final String AUTH_KEY_DB = "AUTH_KEY";
	public static final String AUTH_TS_DB = "AUTH_TS";
	public static final String LAST_ACTION_TS_DB = "LAST_ACTION_TS";
	public static final String LOCKED_ON = "LOCKED_ON";
	public static final String LOGIN_ATTEMPTS = "LOGIN_ATTEMPTS";
	public static final String FIRST_INVALID_LOGIN = "FIRST_INVALID_LOGIN";
	public static final String DEACTIVATION_WARNING = "DEACTIVATION_WARNING";
	public static final String REFERRING_URL_DB = "REFERRING_URL";

	public static final String USER_INTERNAL_ID_ASP = "internalId";
	public static final String USER_ID_ASP = "id";
	public static final String TYPE_ASP = "type";
	public static final String RIGHTS_ASP = "rights";
	public static final String FULL_NAME_ASP = "fullName";
	public static final String EMAIL_ASP = "email";
	public static final String DISPLAY_FULL_NAME_ASP = "displayFullName";
	public static final String STATUS_NAME_ASP = "status";
	public static final String HAS_NOTE_ASP = "hasNote";
	public static final String IS_NARA_STAFF_ASP = "isNaraStaff";
	public static final String ACCOUNT_CREATED_TS_ASP = "accountCreatedTs";
	public static final String RETURN_URL = "returnUrl";
	public static final String RETURN_TEXT = "returnText";
	public static final String REFERRING_URL_ASP = "referringUrl";

	public static final String USER_NAME_ASP = "userName";
	public static final String USER_TYPE_ASP = "userType";
	public static final String USER_RIGHTS_ASP = "userRights";
	public static final String ACTIVATION_CODE_ASP = "activationCode";
	public static final String RESET_CODE_ASP = "resetCode";
	public static final String REASON_ID_ASP = "reasonId";
	public static final String NOTES_ASP = "notes";
	public static final String SHOW_PWD_SET = "showPwdSet";

}
