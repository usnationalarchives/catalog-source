/**
 * 
 */
package gov.nara.opa.api.services.impl.user.notifications;

import gov.nara.opa.api.dataaccess.user.notifications.UserNotificationsDao;
import gov.nara.opa.api.services.user.notifications.UserNotificationsService;
import gov.nara.opa.api.valueobject.user.notifications.UserNotificationValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UserNotificationsServiceImpl implements UserNotificationsService {
	private static OpaLogger logger = OpaLogger
			.getLogger(UserNotificationsServiceImpl.class);

	SecureRandom random;

	@Autowired
	private UserNotificationsDao userNotificationsDao;

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
	@Override
	public List<UserNotificationValueObject> viewNotifications(int accountId,
			int offset, int rows) {
		List<UserNotificationValueObject> resultList = null;

		try {
			logger.info("START - Retrieve notifications");
			// Call the JdcTemplate method to select the list filtering by name.
			resultList = userNotificationsDao.select(accountId, offset, rows);
			logger.info("FINISH - Retrieve notifications");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		}

		return resultList;
	}

	/**
	 * Clear their viewed notifications of the current user.
	 * 
	 * @param accountId
	 *            The accountId we are filtering by.
	 * @return Collection of notifications for the specified accountId
	 */
	@Override
	public boolean clearNotifications(int accountId) {
		boolean result = true;
		try {
			// Call the JdcTemplate method to select the list filtering by name.
			result = userNotificationsDao.clearNotifications(accountId);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		}

		return result;
	}
}
