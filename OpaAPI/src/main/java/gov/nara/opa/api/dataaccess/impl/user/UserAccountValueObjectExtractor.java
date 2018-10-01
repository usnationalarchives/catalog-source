package gov.nara.opa.api.dataaccess.impl.user;

import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

/**
 * ResultSetExtractor implementation for User Account
 */
@Component
public class UserAccountValueObjectExtractor implements
    ResultSetExtractor<UserAccountValueObject>, UserAccountValueObjectConstants {

  @Override
  public UserAccountValueObject extractData(ResultSet rs) throws SQLException,
      DataAccessException {
    UserAccountValueObject userAccount = new UserAccountValueObject();

    userAccount.setAccountCreatedTS(rs.getTimestamp(ACCOUNT_CREATED_TS_DB));
    userAccount.setAccountId(rs.getInt(ACCOUNT_ID_DB));
    userAccount.setAccountNoteFlag(rs.getBoolean(ACCOUNT_NOTE_FLAG_DB));
    userAccount.setAccountReasonFlag(rs.getBoolean(ACCOUNT_REASON_FLAG_DB));
    userAccount.setAccountRights(rs.getString(ACCOUNT_RIGHTS_DB));
    userAccount.setAccountStatus(rs.getBoolean(ACCOUNT_STATUS_DB));
    userAccount.setAccountType(rs.getString(ACCOUNT_TYPE_DB));
    userAccount.setAuthKey(rs.getString(AUTH_KEY_DB));
    userAccount.setAuthTS(rs.getTimestamp(AUTH_TS_DB));
    userAccount.setDisplayFullName(rs.getBoolean(DISPLAY_NAME_FLAG_DB));
    userAccount.setEmailAddress(rs.getString(EMAIL_ADDRESS_DB));
    userAccount.setAdditionalEmailAddress(rs.getString(ADDITIONAL_EMAIL_ADDRESS_DB));
    userAccount.setAdditionalEmailAddressTs(rs.getTimestamp(ADDITIONAL_EMAIL_ADDRESS_TS_DB));
    userAccount.setFullName(rs.getString(FULL_NAME_DB));
    userAccount.setLastActionTS(rs.getTimestamp(LAST_ACTION_TS_DB));
    userAccount.setLastNotificationId(rs.getInt(LAST_NOTIFICATION_ID_DB));
    userAccount.setNaraStaff(rs.getBoolean(IS_NARA_STAFF_DB));
    userAccount.setpWord(rs.getString(P_WORD_DB));
    userAccount.setpWordChangeId(rs.getInt(P_WORD_CHANGE_ID_DB));
    userAccount.setpWordChangeTS(rs.getTimestamp(P_WORD_CHANGE_TS_DB));
    userAccount.setUserName(rs.getString(USER_NAME_DB));
    userAccount.setVerificationGuid(rs.getString(VERIFICATION_GUID_DB));
    userAccount.setVerificationTS(rs.getTimestamp(VERIFICATION_TS_DB));
    userAccount.setLockedOn(rs.getTimestamp(LOCKED_ON));
    userAccount.setLoginAttempts(rs.getInt(LOGIN_ATTEMPTS));
    userAccount.setFirstInvalidLogin(rs.getTimestamp(FIRST_INVALID_LOGIN));
    userAccount.setReferringUrl(rs.getString(REFERRING_URL_DB));

    return userAccount;
  }
}
