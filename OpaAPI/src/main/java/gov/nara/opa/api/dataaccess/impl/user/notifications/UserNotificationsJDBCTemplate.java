package gov.nara.opa.api.dataaccess.impl.user.notifications;

import gov.nara.opa.api.dataaccess.user.notifications.UserNotificationsDao;
import gov.nara.opa.api.valueobject.user.notifications.UserNotificationValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationsJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements UserNotificationsDao {

	/**
	 * Get a collection of Notifications filtering by accountId.
	 * 
	 * @param accountId
	 *            The account id that will be used to filter
	 * @param offset
	 *            The offset specifies the offset of the first row to return
	 * @param rows
	 *            The count specifies maximum number of rows to return
	 * @return Collection of Notifications with the specified name
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserNotificationValueObject> select(int accountId, int offset,
			int rows) throws DataAccessException, UnsupportedEncodingException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		inParamMap.put("limOffset", offset);
		inParamMap.put("limRows", rows);

		return (List<UserNotificationValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetNotifications",
						new GenericRowMapper<UserNotificationValueObject>(
								new UserNotificationExtractor()), inParamMap);
	}

	/**
	 * Clear their viewed notifications of the current user.
	 * 
	 * @param accountId
	 *            The accountId we are filtering by.
	 * @return Collection of notifications for the specified accountId
	 */
	@Override
	public boolean clearNotifications(int accountId)
			throws DataAccessException, UnsupportedEncodingException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);

		return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spClearNotifications", inParamMap);
	}
}
