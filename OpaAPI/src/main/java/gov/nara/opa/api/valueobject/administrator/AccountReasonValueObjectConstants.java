package gov.nara.opa.api.valueobject.administrator;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface AccountReasonValueObjectConstants extends
    CommonValueObjectConstants {
  public static final String REASON_ID_DB = "REASON_ID";
  public static final String ACCOUNT_ID_DB = "ACCOUNT_ID";
  public static final String REASON_DB = "REASON";
  public static final String REASON_STATUS_DB = "REASON_STATUS";
  public static final String REASON_ADDED_TS_DB = "REASON_ADDED_TS";

  public static final String REASON_ID_ASP = "@reasonId";
  public static final String ACCOUNT_ID_ASP = "@accountId";
  public static final String REASON_ASP = "@reason";
  public static final String STATUS_ASP = "@status";
  public static final String CREATED_ASP = "@created";

  public static final String TEXT_REQ_ASP = "text";
}
