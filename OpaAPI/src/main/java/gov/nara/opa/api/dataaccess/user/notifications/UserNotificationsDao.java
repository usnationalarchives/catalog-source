package gov.nara.opa.api.dataaccess.user.notifications;

import gov.nara.opa.api.valueobject.user.notifications.UserNotificationValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;

public interface UserNotificationsDao {

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
	List<UserNotificationValueObject> select(int accountId, int offset, int rows)
			throws DataAccessException, UnsupportedEncodingException;

	/**
	 * Clear their viewed notifications of the current user.
	 * 
	 * @param accountId
	 *            The accountId we are filtering by.
	 * @return Collection of notifications for the specified accountId
	 */
	boolean clearNotifications(int accountId) throws DataAccessException,
			UnsupportedEncodingException;
}
