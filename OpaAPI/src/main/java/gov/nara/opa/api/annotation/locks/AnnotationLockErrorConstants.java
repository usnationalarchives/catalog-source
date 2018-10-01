package gov.nara.opa.api.annotation.locks;

import gov.nara.opa.api.system.ErrorConstants;

public class AnnotationLockErrorConstants extends ErrorConstants {
  public static final String lockLimitReached = "The total number of simultaneously locked files allowed for the user has been reached";
  public static final String lockNotFound = "Lock record not found";
  public static final String alreadyLocked = "Record is locked by a different user";
  public static final String invalidAccountId = "Invalid Account Id";
  public static final String emptyStringValue = "%1$s cannot be empty";
  public static final String userAccountNotFound = "User account not found";

}
