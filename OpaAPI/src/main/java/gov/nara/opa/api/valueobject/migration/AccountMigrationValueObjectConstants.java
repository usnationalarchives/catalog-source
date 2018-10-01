package gov.nara.opa.api.valueobject.migration;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface AccountMigrationValueObjectConstants extends
    CommonValueObjectConstants {
  
  public static final String DUPLICATE_USERNAMES = "duplicateUsernames";
  public static final String DUPLICATE_EMAILS = "duplicateEmails";
  public static final String TOTAL_ACCOUNTS_READ = "@totalAccountsRead";
  public static final String TOTAL_ACCOUNTS_WRITTEN = "@totalAccountsWritten";
  public static final String TOTAL_ACCOUNTS_MIGRATED = "@totalAccountsMigrated";
  public static final String TOTAL_DUPLICATE_USERNAMES = "@totalDuplicateUsernames";
  public static final String TOTAL_DUPLICATE_EMAILS = "@totalDuplicateEmails";
  public static final String NEW_USERNAMES = "newAccounts";
  public static final String FAILED_ACCOUNTS = "failedAccounts";
  
}
