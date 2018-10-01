package gov.nara.opa.api.dataaccess.impl.user.notifications;

import gov.nara.opa.api.valueobject.user.notifications.UserNotificationValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationExtractor implements
		ResultSetExtractor<UserNotificationValueObject> {

	private static OpaLogger logger = OpaLogger
			.getLogger(UserNotificationExtractor.class);

	@Override
	public UserNotificationValueObject extractData(ResultSet resultSet)
			throws SQLException, DataAccessException {
		UserNotificationValueObject userNotification = new UserNotificationValueObject();

		try {
			userNotification.setAccountId(resultSet.getInt("account_id"));
			userNotification.setLogId(resultSet.getInt("log_id"));
			userNotification.setAnnotationId(resultSet.getInt("annotation_id"));
			userNotification.setObjectId(resultSet.getInt("object_id"));
			userNotification.setPageNum(resultSet.getInt("page_num"));
			userNotification.setTotalPages(resultSet.getInt("total_pages"));
			userNotification.setLastNotificationId(resultSet
					.getInt("last_notification_id"));
			userNotification.setUserName(new String(resultSet
					.getBytes("user_name"), "UTF-8"));
			userNotification.setFullName(new String(resultSet
					.getBytes("full_name"), "UTF-8"));
			userNotification
					.setNaraStaff(resultSet.getBoolean("is_nara_staff"));
			userNotification.setDisplayNameFlag(resultSet
					.getBoolean("display_name_flag"));
			userNotification.setAction(new String(resultSet.getBytes("action"),
					"UTF-8"));
			userNotification.setAnnotationType(new String(resultSet
					.getBytes("annotation_type"), "UTF-8"));
			userNotification.setNaId(new String(resultSet.getBytes("na_id"),
					"UTF-8"));
			userNotification.setOpaTitle(new String(resultSet
					.getBytes("opa_title"), "UTF-8"));
			userNotification.setOpaType(new String(resultSet
					.getBytes("opa_type"), "UTF-8"));
			userNotification.setLogTs(resultSet.getTimestamp("log_ts"));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		}

		return userNotification;
	}
}
