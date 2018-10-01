package gov.nara.opa.api.valueobject.moderator;

public interface OnlineAvailabilityHeaderValueObjectConstants {

	public static final String ACTION_REMOVE = "REMOVE";
	public static final String ACTION_RESTORE = "RESTORE";
	public static final String ACTION_UPDATE = "UPDATE";

	public static final String NA_ID_DB = "NA_ID";
	public static final String HEADER_DB = "HEADER";
	public static final String STATUS_DB = "STATUS";
	public static final String TITLE_DB = "OPA_TITLE";
	public static final String ONLINE_AVAILABILITY_TS_DB = "ONLINE_AVAILABILITY_TS";

	public static final String USER_NAME_DB = "USER_NAME";
	public static final String FULL_NAME_DB = "FULL_NAME";
	public static final String DISPLAY_NAME_FLAG_DB = "DISPLAY_NAME_FLAG";
	public static final String IS_NARA_STAFF_DB = "IS_NARA_STAFF";
	public static final String ACTION_DB = "ACTION";
	public static final String LOG_TS_DB = "LOG_TS";

	public static final String VIEW_ONLINE_AVAILABILITY_HEADER = "VIEW_ONLINE_AVAILABILITY_HEADER";
	public static final String ADD_ONLINE_AVAILABILITY_HEADER = "ADD_ONLINE_AVAILABILITY_HEADER";
	public static final String UPDATE_ONLINE_AVAILABILITY_HEADER = "UPDATE_ONLINE_AVAILABILITY_HEADER";
	public static final String REMOVE_ONLINE_AVAILABILITY_HEADER = "REMOVE_ONLINE_AVAILABILITY_HEADER";
	public static final String RESTORE_ONLINE_AVAILABILITY_HEADER = "RESTORE_ONLINE_AVAILABILITY_HEADER";

	public static final String ONLINE_AVAILABILITY_HEADER_NA_ID_ASP = "@naId";
	public static final String ONLINE_AVAILABILITY_HEADER_TITLE_ASP = "@title";
	public static final String ONLINE_AVAILABILITY_HEADER_HEADER_ASP = "@header";
	public static final String ONLINE_AVAILABILITY_HEADER_STATUS_ASP = "@enabled";
	public static final String ONLINE_AVAILABILITY_HEADER_TIMESTAMP_ASP = "@timestamp";
	public static final String ONLINE_AVAILABILITY_HEADER_ACTIONS_ASP = "actions";

	public static final String ONLINE_AVAILABILITY_HEADER_USER_ID_ASP = "@userId";
	public static final String ONLINE_AVAILABILITY_HEADER_FULL_NAME_ASP = "@fullName";
	public static final String ONLINE_AVAILABILITY_HEADER_DISPLAY_NAME_FLAG_ASP = "@displayFullName";
	public static final String ONLINE_AVAILABILITY_HEADER_IS_NARA_STAFF_ASP = "@isNaraStaff";
	public static final String ONLINE_AVAILABILITY_HEADER_ACTION_ASP = "@action";
	public static final String ONLINE_AVAILABILITY_HEADER_ACTION_TS_ASP = "@actionTS";
}
