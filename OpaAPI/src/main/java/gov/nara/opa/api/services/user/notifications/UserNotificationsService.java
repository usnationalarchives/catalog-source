/**
 * 
 */
package gov.nara.opa.api.services.user.notifications;

import gov.nara.opa.api.valueobject.user.notifications.UserNotificationValueObject;

import java.util.List;

public interface UserNotificationsService {

	/**
	 * Retrieve the collection of notifications for this accountId
	 * 
	 * @param accountId
	 *            The accountId we are filtering by.
	 * @param offset
	 *            The offset specifies the offset of the first row to return
	 * @param rows
	 *            The count specifies maximum number of rows to return
	 * @return Collection of notifications for the specified accountId
	 */
	public List<UserNotificationValueObject> viewNotifications(int accountId,
			int offset, int rows);

	/**
	 * Clear their viewed notifications of the current user.
	 * 
	 * @param accountId
	 *            The accountId we are filtering by.
	 * @return Collection of notifications for the specified accountId
	 */
	public boolean clearNotifications(int accountId);
}
