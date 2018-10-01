package gov.nara.opa.api.valueobject.user.notifications;

import gov.nara.opa.api.services.ErrorCode;

/**
 * Enum file used to manage the possible error that the List catalog may return.
 */
public enum UserNotificationErrorCode implements ErrorCode {
	NONE, NOT_API_LOGGED_IN, NOTIFICATIONS_NOT_FOUND, INVALID_VALUE, INVALID_PARAMETER;

	private String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}