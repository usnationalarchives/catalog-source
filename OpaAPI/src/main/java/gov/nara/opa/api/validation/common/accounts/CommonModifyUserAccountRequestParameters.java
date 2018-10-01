package gov.nara.opa.api.validation.common.accounts;

public interface CommonModifyUserAccountRequestParameters {

  public static final int USER_REQUEST = 1;
  public static final int ADMIN_REQUEST = 2;

  public String getUserName();

  public String getPassword();

  public String getNewPassword();

  public String getFullName();

  public Boolean getDisplayFullName();

  public String getEmail();

  public int getRequestType();

  public String getUserType();

  public String getUserRights();
}
