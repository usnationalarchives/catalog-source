package gov.nara.opa.api.user.accounts;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

/**
 * Utility class to handle interaction between AspireObject and UserAccount
 */
@Component
public class UserAccountResponseValuesHelper {

  /**
   * Builds a linked hash map with the required response values. The returned
   * list is not comprehensive.
   * 
   * @param userAccount
   *          The user account instance where the values are extracted
   * @return A linked hash map with the user account values
   */
  public LinkedHashMap<String, Object> getResponseValues(UserAccount userAccount) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

    result.put("internalId", Integer.toString(userAccount.getAccountId()));
    result.put("id", userAccount.getUserName());
    result.put("type", userAccount.getAccountType());
    result.put("rights", userAccount.getAccountRights());
    result.put("fullName", userAccount.getFullName());
    result.put("email", userAccount.getEmailAddress());
    result.put("displayFullName", (userAccount.displayNameFlag ? "true"
        : "false"));
    result.put("status", (userAccount.accountStatus == 1 ? "active"
        : "inactive"));
    result.put("hasNote", (userAccount.isAccountNoteFlag() ? "true" : "false"));
    result.put("isNaraStaff", (userAccount.isNaraStaff() ? "true" : "false"));
    if (userAccount.getAccountCreatedTS() != null)
      result.put("accountCreatedTs", userAccount.getAccountCreatedTS()
          .toString());

    return result;
  }

}
