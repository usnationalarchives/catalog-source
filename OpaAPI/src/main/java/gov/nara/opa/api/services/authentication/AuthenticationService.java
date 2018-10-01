package gov.nara.opa.api.services.authentication;

import gov.nara.opa.api.user.accounts.UserAccount;

public interface AuthenticationService {

  /**
   * Method to validate a user login request
   * 
   * @param userName
   *          login username
   * @param pwd
   *          login password
   * @return Login Object
   */
  public UserAccount authenticateLogin(String userName, String pwd);
}
