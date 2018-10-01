package gov.nara.opa.common.valueobject.user.accounts;

import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserAccountValueObject extends AbstractWebEntityValueObject 
	implements UserAccountValueObjectConstants {

	private Integer accountId;
	private String accountType;
	private String accountRights;
	private String userName;
	private String fullName;
	private Boolean displayFullName = false;
	private String emailAddress;
	private String additionalEmailAddress;
	private Timestamp additionalEmailAddressTs;
	private Boolean isNaraStaff = false;
	private String pWord;
	private Integer lastNotificationId;
	private Boolean accountStatus = false;
	private Boolean accountReasonFlag = false;
	private Boolean accountNoteFlag = false;
	private Timestamp accountCreatedTS;
	private Integer pWordChangeId;
	private Timestamp pWordChangeTS;
	private String verificationGuid;
	private Timestamp verificationTS;
	private Timestamp lastActionTS;
	private Boolean authenticated;
	private String authKey;
	private Timestamp authTS;
	private Integer loginAttempts;
	private Timestamp lockedOn;
	private Timestamp firstInvalidLogin;
	private Integer deactivationWarning;
	private String referringUrl;

	public UserAccountValueObject() {

	}

	public Timestamp getFirstInvalidLogin() {
		return firstInvalidLogin;
	}

	public void setFirstInvalidLogin(Timestamp firstInvalidLogin) {
		this.firstInvalidLogin = firstInvalidLogin;
	}

	public Integer getLoginAttempts() {
		return loginAttempts;
	}

	public void setLoginAttempts(Integer loginAttempts) {
		this.loginAttempts = loginAttempts;
	}

	public Timestamp getLockedOn() {
		return lockedOn;
	}

	public void setLockedOn(Timestamp lockedOn) {
		this.lockedOn = lockedOn;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
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

	public Boolean getDisplayFullName() {
		return displayFullName;
	}

	public void setDisplayFullName(Boolean displayFullName) {
		this.displayFullName = displayFullName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getAdditionalEmailAddress() {
		return additionalEmailAddress;
	}

	public void setAdditionalEmailAddress(String emailAddress) {
		additionalEmailAddress = emailAddress;
	}

	public Timestamp getAdditionEmailAddressTs() {
		return additionalEmailAddressTs;
	}

	public void setAdditionalEmailAddressTs(Timestamp additionalEmailAddressTs) {
		this.additionalEmailAddressTs = additionalEmailAddressTs;
	}

	public Boolean isNaraStaff() {
		return isNaraStaff;
	}

	public void setNaraStaff(Boolean isNaraStaff) {
		this.isNaraStaff = isNaraStaff;
	}

	public String getpWord() {
		return pWord;
	}

	public void setpWord(String pWord) {
		this.pWord = pWord;
	}

	public Integer getLastNotificationId() {
		return lastNotificationId;
	}

	public void setLastNotificationId(Integer lastNotificationId) {
		this.lastNotificationId = lastNotificationId;
	}

	public Boolean getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(Boolean accountStatus) {
		this.accountStatus = accountStatus;
	}

	public Boolean isAccountReasonFlag() {
		return accountReasonFlag;
	}

	public void setAccountReasonFlag(Boolean accountReasonFlag) {
		this.accountReasonFlag = accountReasonFlag;
	}

	public Boolean isAccountNoteFlag() {
		return accountNoteFlag;
	}

	public void setAccountNoteFlag(Boolean accountNoteFlag) {
		this.accountNoteFlag = accountNoteFlag;
	}

	public Timestamp getAccountCreatedTS() {
		return accountCreatedTS;
	}

	public void setAccountCreatedTS(Timestamp accountCreatedTS) {
		this.accountCreatedTS = accountCreatedTS;
	}

	public Integer getpWordChangeId() {
		return pWordChangeId;
	}

	public void setpWordChangeId(Integer pWordChangeId) {
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

	public Boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(Boolean authenticated) {
		this.authenticated = authenticated;
	}

	public Boolean isModeratorType() {
		return (this.getAccountType().equals("moderator"));
	}

	public Boolean isAdministratorType() {
		return (this.getAccountType().equals("power"));
	}

	public Boolean isStandardType() {
		return (this.getAccountType().equals("standard"));
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public Timestamp getAuthTS() {
		return authTS;
	}

	public void setAuthTS(Timestamp authTS) {
		this.authTS = authTS;
	}

	public String getDisplayUserName() {
		return getDisplayFullName().booleanValue() ? getFullName() : getUserName();
	}

	public Integer getDeactivationWarning() {
		return deactivationWarning;
	}

	public void setDeactivationWarning(Integer deactivationWarning) {
		this.deactivationWarning = deactivationWarning;
	}

	public String getReferringUrl() {
		return referringUrl;
	}

	public void setReferringUrl(String referringUrl) {
		this.referringUrl = referringUrl;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		result.put(USER_INTERNAL_ID_ASP, getAccountId());
		result.put(USER_ID_ASP, getUserName());
		result.put(TYPE_ASP, getAccountType());
		result.put(RIGHTS_ASP, getAccountRights());
		result.put(FULL_NAME_ASP, getFullName());
		result.put(EMAIL_ASP, getEmailAddress());
		result.put(DISPLAY_FULL_NAME_ASP, getDisplayFullName());
		result.put(STATUS_NAME_ASP, (getAccountStatus() ? ACTIVE_CODE
				: INACTIVE_CODE));
		result.put(HAS_NOTE_ASP, isAccountNoteFlag());
		result.put(IS_NARA_STAFF_ASP, isNaraStaff());
		result.put(ACCOUNT_CREATED_TS_ASP,
				TimestampUtils.getUtcString(getAccountCreatedTS()));
		if(!StringUtils.isNullOrEmtpy(getReferringUrl())) {
			result.put(REFERRING_URL_ASP, getReferringUrl());
		}
		return result;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new LinkedHashMap<String, Object>();
		databaseContent.put(ACCOUNT_CREATED_TS_DB, getAccountCreatedTS());
		databaseContent.put(ACCOUNT_ID_DB, getAccountId());
		databaseContent.put(ACCOUNT_NOTE_FLAG_DB, isAccountNoteFlag());
		databaseContent.put(ACCOUNT_REASON_FLAG_DB, isAccountReasonFlag());
		databaseContent.put(ACCOUNT_RIGHTS_DB, getAccountRights());
		databaseContent.put(ACCOUNT_STATUS_DB, getAccountStatus());
		databaseContent.put(ACCOUNT_TYPE_DB, getAccountType());
		databaseContent.put(AUTH_KEY_DB, getAuthKey());
		databaseContent.put(AUTH_TS_DB, getAuthTS());
		databaseContent.put(DISPLAY_NAME_FLAG_DB, getDisplayFullName());
		databaseContent.put(EMAIL_ADDRESS_DB, getEmailAddress());
		databaseContent.put(ADDITIONAL_EMAIL_ADDRESS_DB, getAdditionalEmailAddress());
		databaseContent.put(FULL_NAME_DB, getFullName());
		databaseContent.put(IS_NARA_STAFF_DB, isNaraStaff());
		databaseContent.put(LAST_ACTION_TS_DB, getLastActionTS());
		databaseContent.put(LAST_NOTIFICATION_ID_DB, getLastNotificationId());
		databaseContent.put(P_WORD_CHANGE_ID_DB, getpWordChangeId());
		databaseContent.put(P_WORD_CHANGE_TS_DB, getpWordChangeTS());
		databaseContent.put(P_WORD_DB, getpWord());
		databaseContent.put(USER_NAME_DB, getUserName());
		databaseContent.put(VERIFICATION_GUID_DB, getVerificationGuid());
		databaseContent.put(VERIFICATION_TS_DB, getVerificationTS());
		databaseContent.put(DEACTIVATION_WARNING, getDeactivationWarning());
		databaseContent.put(REFERRING_URL_DB, getReferringUrl());
		return databaseContent;
	}
}
