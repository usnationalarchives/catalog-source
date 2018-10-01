package gov.nara.opa.api.user.accounts;

import gov.nara.opa.api.system.ErrorConstants;

public class UserAccountErrorConstants extends ErrorConstants {
  public static final String valueSizeTooSmall = "Size of '%1$s' (%2$d) is less than required size: %3$d";
  public static final String passwordAndVerificationMismatch = "Password and verification password don't match";
  public static final String invalidUserType = "Invalid user type '%1$s'. Only 'power' or 'standard' allowed";
  public static final String emptyStringValue = "Please fill in %1$s";
  public static final String invalidAction = "Invalid Action";
  public static final String invalidEmailAddress = "Invalid email address: '%1$s'";
  public static final String userNameExists = "The User Name: %1$s is already used for registration. Please choose another User Name.";
  public static final String emailExists = "The Email Address: %1$s is already used for registration. Please choose another Email Address.";
  public static final String accountInactive = "The account is inactive";
  public static final String emailNotExists = "Email address submitted is not in the system. Please check.";
  public static final String userNotExists = "User account not found";
  public static final String invalidUserName = "Invalid Username";
  public static final String invalidActivationCode = "Invalid activation code";
  public static final String noParameterForModification = "No parameter specified for the requested change";
  public static final String invalidCredentials = "Invalid credentials";
  public static final String invalidResetCode = "Invalid reset code";
  public static final String unableToInsert = "Unable to insert user account";
  public static final String internalDatabaseError = "Internal database error";
  public static final String unableToUpdate = "Unable to update user account";
  public static final String invalidPassword = "Invalid password";
  public static final String missingPassword = "Missing password";
  public static final String fillInUserName = "Please fill in Username";
  public static final String accountLocked = "Too many failed login attempts. Please try again later.";

}
