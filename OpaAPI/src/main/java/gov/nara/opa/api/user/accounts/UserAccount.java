package gov.nara.opa.api.user.accounts;

import java.io.Serializable;
import java.sql.Timestamp;

public class UserAccount implements Serializable {

  private static final long serialVersionUID = 1L;

  int accountId;
  String accountType;
  String accountRights;
  String userName;
  String fullName;
  boolean displayNameFlag;
  String emailAddress;
  boolean isNaraStaff;
  String pWord;
  int lastNotificationId;
  int accountStatus;
  int accountReasonFlag;
  boolean accountNoteFlag;
  Timestamp accountCreatedTS;
  int pWordChangeId;
  Timestamp pWordChangeTS;
  String verificationGuid;
  Timestamp verificationTS;
  Timestamp lastActionTS;
  Timestamp lockedOn;
  int loginAttempts;
  Timestamp firstInvalidLogin;
  boolean authenticated;
  UserAccountErrorCode errorCode;
  String errorMessage;

  public UserAccount() {

  }

  public UserAccount(String userName, String accountType, String accountRights,
      String fullName, boolean displayNameFlag, String pWord,
      String emailAddress) {
    this.userName = userName;
    this.accountType = accountType;
    this.accountRights = accountRights;
    this.fullName = fullName;
    this.displayNameFlag = displayNameFlag;
    this.pWord = pWord;
    this.emailAddress = emailAddress;
  }

  public UserAccount(int accountId, String userName, String accountType,
      String fullName, boolean displayNameFlag, String pWord,
      String emailAddress, boolean authenticated, Timestamp lastActionTS,
      boolean accountNoteFlag, boolean isNaraStaff, Timestamp accountCreatedTS,
      String accountRights, int accountStatus, Timestamp passwordChangeTS,
      String verificationGuid, Timestamp verificationTS, int passwordChangeId,
      Timestamp lockedOn, int loginAttempts, Timestamp firstInvalidLogin,
      UserAccountErrorCode errorCode, String errorMessage) {
    this.accountId = accountId;
    this.userName = userName;
    this.accountType = accountType;
    this.fullName = fullName;
    this.displayNameFlag = displayNameFlag;
    this.pWord = pWord;
    this.emailAddress = emailAddress;
    this.authenticated = authenticated;
    this.lastActionTS = lastActionTS;
    this.accountNoteFlag = accountNoteFlag;
    this.isNaraStaff = isNaraStaff;
    this.accountCreatedTS = accountCreatedTS;
    this.accountRights = accountRights;
    this.accountStatus = accountStatus;
    this.pWordChangeTS = passwordChangeTS;
    this.verificationGuid = verificationGuid;
    this.verificationTS = verificationTS;
    this.pWordChangeId = passwordChangeId;
    this.lockedOn = lockedOn;
    this.loginAttempts = loginAttempts;
    this.firstInvalidLogin = firstInvalidLogin;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public int getAccountId() {
    return accountId;
  }

  public void setAccountId(int accountId) {
    this.accountId = accountId;
  }

  public String getAccountType() {
    return accountType;
  }

  public void setAccountType(String accountType) {
    this.accountType = accountType;
  }

  public String getAccountRights() {
    return accountRights;
  }

  public void setAccountRights(String accountRights) {
    this.accountRights = accountRights;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public boolean isDisplayNameFlag() {
    return displayNameFlag;
  }

  public void setDisplayNameFlag(boolean displayNameFlag) {
    this.displayNameFlag = displayNameFlag;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public boolean isNaraStaff() {
    return isNaraStaff;
  }

  public void setNaraStaff(boolean isNaraStaff) {
    this.isNaraStaff = isNaraStaff;
  }

  public String getpWord() {
    return pWord;
  }

  public void setpWord(String pWord) {
    this.pWord = pWord;
  }

  public int getLastNotificationId() {
    return lastNotificationId;
  }

  public void setLastNotificationId(int lastNotificationId) {
    this.lastNotificationId = lastNotificationId;
  }

  public int getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(int accountStatus) {
    this.accountStatus = accountStatus;
  }

  public int getAccountReasonFlag() {
    return accountReasonFlag;
  }

  public void setAccountReasonFlag(int accountReasonFlag) {
    this.accountReasonFlag = accountReasonFlag;
  }

  public boolean isAccountNoteFlag() {
    return accountNoteFlag;
  }

  public void setAccountNoteFlag(boolean accountNoteFlag) {
    this.accountNoteFlag = accountNoteFlag;
  }

  public Timestamp getAccountCreatedTS() {
    return accountCreatedTS;
  }

  public void setAccountCreatedTS(Timestamp accountCreatedTS) {
    this.accountCreatedTS = accountCreatedTS;
  }

  public int getpWordChangeId() {
    return pWordChangeId;
  }

  public void setpWordChangeId(int pWordChangeId) {
    this.pWordChangeId = pWordChangeId;
  }

  public Timestamp getpWordChangeTS() {
    return pWordChangeTS;
  }

  public void setpWordChangeTS(Timestamp pWordChangeTS) {
    this.pWordChangeTS = pWordChangeTS;
  }

  public String getVerificationGuid() {
    return verificationGuid;
  }

  public void setVerificationGuid(String verificationGuid) {
    this.verificationGuid = verificationGuid;
  }

  public Timestamp getVerificationTS() {
    return verificationTS;
  }

  public void setVerificationTS(Timestamp verificationTS) {
    this.verificationTS = verificationTS;
  }

  public Timestamp getLastActionTS() {
    return lastActionTS;
  }

  public void setLastActionTS(Timestamp lastActionTS) {
    this.lastActionTS = lastActionTS;
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public void setAuthenticated(boolean authenticated) {
    this.authenticated = authenticated;
  }

  public boolean isModerator() {
    return (this.getAccountRights().equals("moderator"));
  }

  public boolean isAdministrator() {
    return (this.getAccountRights().equals("accountAdmin"));
  }

  public boolean isAdminModerator() {
    return (this.getAccountRights().equals("accountAdminMod"));
  }

  public boolean isStandardType() {
    return (this.getAccountType().equals("standard"));
  }

  public boolean isPowerType() {
    return (this.getAccountType().equals("power"));
  }

  public Timestamp getFirstInvalidLogin() {
    return firstInvalidLogin;
  }

  public void setFirstInvalidLogin(Timestamp firstInvalidLogin) {
    this.firstInvalidLogin = firstInvalidLogin;
  }

  public int getLoginAttempts() {
    return loginAttempts;
  }

  public void setLoginAttempts(int loginAttempts) {
    this.loginAttempts = loginAttempts;
  }

  public Timestamp getLockedOn() {
    return lockedOn;
  }

  public void setLockedOn(Timestamp lockedOn) {
    this.lockedOn = lockedOn;
  }

  public UserAccountErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(UserAccountErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String toString() {
    String template = "accountId=%1$d, accountType=%2$s, accountRights=%3$s, userName=%4$s, fullName=\"%5$s\", "
        + "displayNameFlag=%6$b, emailAddress=%7$s, isNaraStaff=%8$b, pWord=%9$s, lastNotificationId=%10$d, "
        + "accountStatus=%11$d, accountReasonFlag=%12$d, accountNoteFlag=%13$b, accountCreatedTS=%14$s, "
        + "pWordChangeId=%15$d, "
        + "pWordChangeTS=%16$s, verificationGuid=%17$s, verificationTS=%18$s, lastActionTS=%19$s, "
        + "authenticated=%20$b";

    return String.format(template, accountId, accountType, accountRights,
        userName, fullName, displayNameFlag, emailAddress, isNaraStaff, pWord,
        lastNotificationId, accountStatus, accountReasonFlag, accountNoteFlag,
        accountCreatedTS, pWordChangeId, pWordChangeTS, verificationGuid,
        verificationTS, lastActionTS, authenticated);
  }

}
