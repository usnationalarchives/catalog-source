package gov.nara.opa.api.valueobject.migration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class AccountsMigrationValueObject extends AbstractWebEntityValueObject 
  implements AccountMigrationValueObjectConstants {

  private ArrayList<String> duplicateUserNames;
  private ArrayList<String> duplicateEmails;
  private LinkedHashMap<String,String> failedAccounts;
  private Integer totalAccountsRead;
  private Integer totalAccountsWritten;
  private Integer totalAccountsMigrated;
  private Integer totalDuplicateUserNames;
  private Integer totalDuplicateEmails;
  
  private Boolean fullDetail = false;
  
  public Boolean getFullDetail() {
    return fullDetail;
  }

  public void setFullDetail(Boolean fullDetail) {
    this.fullDetail = fullDetail;
  }

  
  public Integer getTotalDuplicateUserNames() {
    return totalDuplicateUserNames;
  }

  public void setTotalDuplicateUserNames(Integer totalDuplicateUserNames) {
    this.totalDuplicateUserNames = totalDuplicateUserNames;
  }

  public Integer getTotalDuplicateEmails() {
    return totalDuplicateEmails;
  }

  public void setTotalDuplicateEmails(Integer totalDuplicateEmails) {
    this.totalDuplicateEmails = totalDuplicateEmails;
  }

  public ArrayList<String> getDuplicateUserNames() {
    return duplicateUserNames;
  }

  public void setDuplicateUserNames(ArrayList<String> duplicateUserNames) {
    this.duplicateUserNames = duplicateUserNames;
  }

  public ArrayList<String> getDuplicateEmails() {
    return duplicateEmails;
  }

  public void setDuplicateEmails(ArrayList<String> duplicateEmails) {
    this.duplicateEmails = duplicateEmails;
  }

  public LinkedHashMap<String, String> getFailedAccounts() {
    if(failedAccounts == null) {
      failedAccounts = new LinkedHashMap<String, String>();
    }
    return failedAccounts;
  }

  public void setFailedAccounts(LinkedHashMap<String, String> failedAccounts) {
    this.failedAccounts = failedAccounts;
  }

  public Integer getTotalAccountsRead() {
    return totalAccountsRead;
  }

  public void setTotalAccountsRead(Integer totalAccountsRead) {
    this.totalAccountsRead = totalAccountsRead;
  }

  public Integer getTotalAccountsWritten() {
    return totalAccountsWritten;
  }

  public void setTotalAccountsWritten(Integer totalAccountsWritten) {
    this.totalAccountsWritten = totalAccountsWritten;
  }

  public Integer getTotalAccountsMigrated() {
    return totalAccountsMigrated;
  }

  public void setTotalAccountsMigrated(Integer totalAccountsMigrated) {
    this.totalAccountsMigrated = totalAccountsMigrated;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

    result.put("@action", action);
    result.put(TOTAL_ACCOUNTS_READ, getTotalAccountsRead());
    result.put(TOTAL_ACCOUNTS_WRITTEN, getTotalAccountsWritten());
    result.put(TOTAL_ACCOUNTS_MIGRATED, getTotalAccountsMigrated());
    result.put(TOTAL_DUPLICATE_USERNAMES, getTotalDuplicateUserNames());
    result.put(TOTAL_DUPLICATE_EMAILS, getTotalDuplicateEmails());
    if(fullDetail) {
      result.put(DUPLICATE_USERNAMES, getDuplicateUserNames());
      result.put(DUPLICATE_EMAILS, getDuplicateEmails());
      result.put(FAILED_ACCOUNTS, getFailedAccounts());
    }
    
    return result;
  }

}
