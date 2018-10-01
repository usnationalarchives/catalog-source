package gov.nara.opa.api.valueobject.user.logs;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface AccountLogValueObjectConstants extends
    CommonValueObjectConstants {

  // Constants for the ACCOUNTS_LOG table
  public static final String LOG_ID_DB = "LOG_ID";
  public static final String ACCOUNT_ID_DB = "ACCOUNT_ID";
  public static final String ADMIN_USER_NAME_DERIVED = "ADMIN_USER_NAME";
  public static final String ADMIN_FULL_NAME_DERIVED = "ADMIN_FULL_NAME";
  public static final String ADMIN_ACCOUNT_ID_DB = "ADMIN_ACCOUNT_ID";
  public static final String STATUS_DB = "STATUS";
  public static final String ACTION_DB = "ACTION";
  public static final String REASON_ID_DB = "REASON_ID";
  public static final String NOTES_DB = "NOTES";
  public static final String LOG_TS_DB = "LOG_TS";

  public static final String ADMIN_ID_ASP = "@adminId";
  public static final String ADMIN_USER_NAME_ASP = "@adminUserName";
  public static final String ACTION_ASP = "@action";
  public static final String LOG_TS_ASP = "@when";
  public static final String REASON_ASP = "@reason";
  public static final String REASON_ID_ASP = "@reasonId";
  public static final String NOTE_ASP = "@note";
  public static final String ACCOUNT_STATUS_ASP = "@status";
  public static final String HAS_NOTE_ASP = "@hasNote";
  public static final String ADMIN_FULL_NAME_ASP = "@adminFullName";
}
