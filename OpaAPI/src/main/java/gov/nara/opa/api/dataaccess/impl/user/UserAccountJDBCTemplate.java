package gov.nara.opa.api.dataaccess.impl.user;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.validation.administrator.AdministratorSearchAccountsRequestParameters;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation for UserAccountDao. See this interface for javadocs
 */
@Component
@Transactional
public class UserAccountJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements UserAccountDao, UserAccountValueObjectConstants {

	private static final String SELECT_USER_BASE = "SELECT * FROM accounts ";

	private static final String SELECT_USER_COUNT = "SELECT count(1) total_users FROM accounts ";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#verifyIfUserNameExists
	 * (java.lang.String)
	 */
	@Override
	public boolean verifyIfUserNameExists(String userName) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("userName", userName);
		List<Map<String, Object>> userNames = StoredProcedureDataAccessUtils
				.executeWithListResults(getJdbcTemplate(),
						"spSelectUsernameExists", inParamMap);
		return userNames.size() > 0 ? true : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#create(gov.nara.opa.common
	 * .valueobject.user.accounts.UserAccountValueObject)
	 */
	@Override
	public void create(UserAccountValueObject userAccount) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountType", userAccount.getAccountType());
		inParamMap.put("accountRights", userAccount.getAccountRights());
		inParamMap.put("userName", userAccount.getUserName());
		inParamMap.put("fullName", userAccount.getFullName());
		inParamMap.put("displayFullname", userAccount.getDisplayFullName());
		inParamMap.put("emailAddress", userAccount.getEmailAddress());
		inParamMap.put("additionalEmailAddress",
				userAccount.getAdditionalEmailAddress());
		inParamMap.put("additionalEmailAddressTs",
				userAccount.getAdditionEmailAddressTs());
		inParamMap.put("isNaraStaff", userAccount.isNaraStaff());
		inParamMap.put("pWord", userAccount.getpWord());
		inParamMap.put("lastNotificationId",
				userAccount.getLastNotificationId());
		inParamMap.put("accountStatus", userAccount.getAccountStatus());
		inParamMap.put("accountReasonFlag", userAccount.isAccountReasonFlag());
		inParamMap.put("accountNoteFlag", userAccount.isAccountNoteFlag());
		inParamMap.put("pWordChangeId", userAccount.getpWordChangeId());
		inParamMap.put("pWordChangeTs", userAccount.getpWordChangeTS());
		inParamMap.put("verificationGuid", userAccount.getVerificationGuid());
		inParamMap.put("verificationTs", userAccount.getVerificationTS());
		inParamMap.put("authKey", userAccount.getAuthKey());
		inParamMap.put("authTs", userAccount.getAuthTS());
		inParamMap.put("lastActionTs", TimestampUtils.getUtcTimestamp());
		inParamMap.put("lockedOn", userAccount.getLockedOn());
		inParamMap.put("loginAttempts", userAccount.getLoginAttempts());
		inParamMap.put("firstInvalidLogin", userAccount.getFirstInvalidLogin());
		inParamMap.put("deactivationWarning",
				userAccount.getDeactivationWarning());
		inParamMap.put("referringUrl", userAccount.getReferringUrl());
		
		int accountId = StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(), "spInsertAccount", inParamMap, "accountId");
		userAccount.setAccountId(accountId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#verifyIfEmailExists(java
	 * .lang.String)
	 */
	@Override
	public boolean verifyIfEmailExists(String emailAddress) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("emailAddress", emailAddress);
		List<Map<String, Object>> userEmails = StoredProcedureDataAccessUtils
				.executeWithListResults(getJdbcTemplate(),
						"spSelectEmailExists", inParamMap);
		return userEmails.size() > 0 ? true : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.api.dataaccess.user.UserAccountDao#
	 * getUserAccountByActivationCode(java.lang.String)
	 */
	@Override
	public UserAccountValueObject getUserAccountByActivationCode(
			String activationCode) {
		return selectByUniqueIdentifier(VERIFICATION_GUID_DB,
				new Object[] { activationCode });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#selectByUserName(java
	 * .lang.String)
	 */
	@Override
	public UserAccountValueObject selectByUserName(String userName) {
		return selectByUniqueIdentifier(USER_NAME_DB, new Object[] { userName });
	}

	private UserAccountValueObject selectByUniqueIdentifier(String columnName,
			Object[] parameterValue) {
		String sql = SELECT_USER_BASE + " WHERE " + columnName + " = ?";

		List<UserAccountValueObject> users = getJdbcTemplate().query(sql,
				parameterValue, new UserAccountValueObjectRowMapper());

		return (users != null && !users.isEmpty() ? users.get(0) : null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#selectByEmail(java.lang
	 * .String)
	 */
	@Override
	public UserAccountValueObject selectByEmail(String email) {
		return selectByUniqueIdentifier(EMAIL_ADDRESS_DB,
				new Object[] { email });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#selectByAccountId(int)
	 */
	@Override
	public UserAccountValueObject selectByAccountId(int accountId) {
		return selectByUniqueIdentifier(ACCOUNT_ID_DB,
				new Object[] { new Integer(accountId) });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#getCurrentPassword(java
	 * .lang.String)
	 */
	@Override
	public String getCurrentPassword(String userName) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("userName", userName);
		String pword = null;
		StoredProcedureDataAccessUtils.executeWithStringResult(
				getJdbcTemplate(), "spSelectPassword", inParamMap, pword);
		return pword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#clearLoginAttemptsInfo
	 * (java.lang.String)
	 */
	@Override
	public void clearLoginAttemptsInfo(String userName) {
		String sql = "UPDATE accounts SET login_attempts = null, locked_on = null, "
				+ "first_invalid_login = null WHERE user_name = ? ";
		getJdbcTemplate().update(sql, new Object[] { userName });

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#updateLoginAttemptsInfo
	 * (java.lang.String, boolean, int)
	 */
	@Override
	public void updateLoginAttemptsInfo(String userName, boolean lockAccount,
			int loginAttempts) {
		String sql = "";
		if (!lockAccount) {
			sql = "UPDATE accounts SET login_attempts = "
					+ loginAttempts
					+ ", locked_on = null, first_invalid_login = now() WHERE user_name = ? ";
		} else {
			sql = "UPDATE accounts SET login_attempts = "
					+ loginAttempts
					+ ", locked_on = now(), first_invalid_login = now() WHERE user_name = ? ";
		}

		getJdbcTemplate().update(sql, new Object[] { userName });

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#updateDeactivationWarning
	 * (gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject)
	 */
	public void updateDeactivationWarning(UserAccountValueObject userAccount) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", userAccount.getAccountId());
		inParamMap.put("deactivationWarning",
				userAccount.getDeactivationWarning());
		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spUpdateDeactivationWarning", inParamMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.api.dataaccess.user.UserAccountDao#
	 * migrationSelectByTempAccountId(int)
	 */
	@Override
	public UserAccountValueObject migrationSelectByTempAccountId(
			int tmpListaccountId) {
		String sql = SELECT_USER_BASE
				+ " WHERE user_name = (select UM_USER_NAME from migration.UM_USERS where UM_USER_ID = ? LIMIT 1) ";

		List<UserAccountValueObject> users = getJdbcTemplate().query(sql,
				new Object[] { tmpListaccountId },
				new UserAccountValueObjectRowMapper());

		return (users != null && !users.isEmpty() ? users.get(0) : null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#update(gov.nara.opa.common
	 * .valueobject.user.accounts.UserAccountValueObject)
	 */
	@Override
	public void update(UserAccountValueObject userAccount) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", userAccount.getAccountId());
		inParamMap.put("accountType", userAccount.getAccountType());
		inParamMap.put("accountRights", userAccount.getAccountRights());
		inParamMap.put("userName", userAccount.getUserName());
		inParamMap.put("fullName", userAccount.getFullName());
		inParamMap.put("displayFullname", userAccount.getDisplayFullName());
		inParamMap.put("emailAddress", userAccount.getEmailAddress());
		inParamMap.put("additionalEmailAddress",
				userAccount.getAdditionalEmailAddress());
		inParamMap.put("additionalEmailAddressTs",
				userAccount.getAdditionEmailAddressTs());
		inParamMap.put("isNaraStaff", userAccount.isNaraStaff());
		inParamMap.put("pWord", userAccount.getpWord());
		inParamMap.put("lastNotificationId",
				userAccount.getLastNotificationId());
		inParamMap.put("accountStatus", userAccount.getAccountStatus());
		inParamMap.put("accountReasonFlag", userAccount.isAccountReasonFlag());
		inParamMap.put("accountNoteFlag", userAccount.isAccountNoteFlag());
		inParamMap.put("accountCreatedTs", userAccount.getAccountCreatedTS());
		inParamMap.put("pWordChangeId", userAccount.getpWordChangeId());
		inParamMap.put("pWordChangeTs", userAccount.getpWordChangeTS());
		inParamMap.put("verificationGuid", userAccount.getVerificationGuid());
		inParamMap.put("verificationTs", userAccount.getVerificationTS());
		inParamMap.put("authKey", userAccount.getAuthKey());
		inParamMap.put("authTs", userAccount.getAuthTS());
		inParamMap.put("lastActionTs", userAccount.getLastActionTS());
		inParamMap.put("lockedOn", userAccount.getLockedOn());
		inParamMap.put("loginAttempts", userAccount.getLoginAttempts());
		inParamMap.put("firstInvalidLogin", userAccount.getFirstInvalidLogin());
		inParamMap.put("deactivationWarning",
				userAccount.getDeactivationWarning());
		inParamMap.put("referringUrl", userAccount.getReferringUrl());
		
		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spUpdateAccount", inParamMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#search(gov.nara.opa.api
	 * .validation.administrator.AdministratorSearchAccountsRequestParameters)
	 */
	@Override
	public List<UserAccountValueObject> search(
			AdministratorSearchAccountsRequestParameters requestParameters) {

		StringBuffer sql = new StringBuffer(SELECT_USER_BASE);
		ArrayList<Object> params = new ArrayList<Object>();

		if (requestParameters.getId() != null
				|| requestParameters.getFullName() != null
				|| requestParameters.getEmail() != null
				|| requestParameters.getUserRights() != null
				|| requestParameters.getUserStatus() != null
				|| requestParameters.getUserType() != null
				|| requestParameters.getDisplayFullName() != null
				|| requestParameters.getHasNotes() != null
				|| requestParameters.getInternalId() != null) {
			sql.append(" WHERE ");
		}

		appendAndParamToSql(USER_NAME_DB, requestParameters.getId(), sql,
				params, true);
		appendAndParamToSql(FULL_NAME_DB, requestParameters.getFullName(), sql,
				params, true);
		appendAndParamToSql(EMAIL_ADDRESS_DB, requestParameters.getEmail(),
				sql, params, true);
		appendAndParamToSql(ACCOUNT_ID_DB, requestParameters.getInternalId(),
				sql, params, true, false, true);
		appendAndParamToSql(ACCOUNT_RIGHTS_DB,
				requestParameters.getUserRights(), sql, params);
		appendAndParamToSql(ACCOUNT_TYPE_DB, requestParameters.getUserType(),
				sql, params);
		appendAndParamToSql(DISPLAY_NAME_FLAG_DB,
				requestParameters.getDisplayFullName(), sql, params);
		appendAndParamToSql(ACCOUNT_NOTE_FLAG_DB,
				requestParameters.getHasNotes(), sql, params);
		if (requestParameters.getStatus() == null) {
			if (params.size() > 0) {
				sql.append(" AND");
			}
			sql.append(String.format(" (%s = 1 OR %s = 0)", ACCOUNT_STATUS_DB,
					ACCOUNT_STATUS_DB));
		} else {
			appendAndParamToSql(ACCOUNT_STATUS_DB,
					requestParameters.getUserStatus(), sql, params);
		}

		// Invert sorting of state due to textual value (active/inactive)
		if (requestParameters.getSortField() != null
				&& requestParameters.getSortField().equals(ACCOUNT_STATUS_DB)) {
			requestParameters.setSortDirection((requestParameters
					.getSortDirection().equals("DESC") ? "ASC" : "DESC"));
		}

		appendSearchRequestClauses(sql, requestParameters);

		return getJdbcTemplate().query(sql.toString(), params.toArray(),
				new UserAccountValueObjectRowMapper());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#getSearchTotalResults
	 * (gov.nara.opa.api.validation.administrator.
	 * AdministratorSearchAccountsRequestParameters)
	 */
	@Override
	public int getSearchTotalResults(
			AdministratorSearchAccountsRequestParameters requestParameters) {

		StringBuffer sql = new StringBuffer(SELECT_USER_COUNT);
		ArrayList<Object> params = new ArrayList<Object>();

		if (requestParameters.getId() != null
				|| requestParameters.getFullName() != null
				|| requestParameters.getEmail() != null
				|| requestParameters.getUserStatus() != null
				|| requestParameters.getUserRights() != null
				|| requestParameters.getUserType() != null
				|| requestParameters.getDisplayFullName() != null
				|| requestParameters.getHasNotes() != null
				|| requestParameters.getInternalId() != null) {
			sql.append(" WHERE ");
		}

		appendAndParamToSql(USER_NAME_DB, requestParameters.getId(), sql,
				params, true);
		appendAndParamToSql(FULL_NAME_DB, requestParameters.getFullName(), sql,
				params, true);
		appendAndParamToSql(EMAIL_ADDRESS_DB, requestParameters.getEmail(),
				sql, params, true);
		appendAndParamToSql(ACCOUNT_STATUS_DB,
				requestParameters.getUserStatus(), sql, params);
		appendAndParamToSql(ACCOUNT_ID_DB, requestParameters.getInternalId(),
				sql, params, true, false, true);
		appendAndParamToSql(ACCOUNT_RIGHTS_DB,
				requestParameters.getUserRights(), sql, params);
		appendAndParamToSql(ACCOUNT_TYPE_DB, requestParameters.getUserType(),
				sql, params);
		appendAndParamToSql(DISPLAY_NAME_FLAG_DB,
				requestParameters.getDisplayFullName(), sql, params);
		appendAndParamToSql(ACCOUNT_NOTE_FLAG_DB,
				requestParameters.getHasNotes(), sql, params);

		appendSearchRequestClauses(sql, requestParameters, false);

		return getJdbcTemplate().queryForInt(sql.toString(), params.toArray());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.api.dataaccess.user.UserAccountDao#getTotalUserCount()
	 */
	@Override
	public int getTotalUserCount() {
		return getJdbcTemplate().queryForInt(SELECT_USER_COUNT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.api.dataaccess.user.UserAccountDao#delete(gov.nara.opa.common
	 * .valueobject.user.accounts.UserAccountValueObject)
	 */
	@Override
	public void delete(int accountId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spDeleteUserAccount", inParamMap);
	}
}
