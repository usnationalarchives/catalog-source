package gov.nara.opa.api.valueobject.user.notifications;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class UserNotificationValueObject extends AbstractWebEntityValueObject {
	private int accountId;
	private int logId;
	private int annotationId;
	private int objectId;
	private int pageNum;
	private int totalPages;
	private int lastNotificationId;
	private boolean isNaraStaff;
	private boolean displayNameFlag;
	private String userName;
	private String fullName;
	private String action;
	private String annotationType;
	private String naId;
	private String opaTitle;
	private String opaType;
	private Timestamp logTs;

	public UserNotificationValueObject() {
	}

	/**
	 * @return the lastNotificationId
	 */
	public int getLastNotificationId() {
		return lastNotificationId;
	}

	/**
	 * @param lastNotificationId
	 *            the lastNotificationId to set
	 */
	public void setLastNotificationId(int lastNotificationId) {
		this.lastNotificationId = lastNotificationId;
	}

	/**
	 * @return the totalPages
	 */
	public int getTotalPages() {
		return totalPages;
	}

	/**
	 * @param totalPages
	 *            the totalPages to set
	 */
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * @return the isNaraStaff
	 */
	public boolean isNaraStaff() {
		return isNaraStaff;
	}

	/**
	 * @param isNaraStaff
	 *            the isNaraStaff to set
	 */
	public void setNaraStaff(boolean isNaraStaff) {
		this.isNaraStaff = isNaraStaff;
	}

	/**
	 * @return the displayNameFlag
	 */
	public boolean getDisplayNameFlag() {
		return displayNameFlag;
	}

	/**
	 * @param displayNameFlag
	 *            the displayNameFlag to set
	 */
	public void setDisplayNameFlag(boolean displayNameFlag) {
		this.displayNameFlag = displayNameFlag;
	}

	/**
	 * @return the opaType
	 */
	public String getOpaType() {
		return opaType;
	}

	/**
	 * @param opaType
	 *            the opaType to set
	 */
	public void setOpaType(String opaType) {
		this.opaType = opaType;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the annotationId
	 */
	public int getAnnotationId() {
		return annotationId;
	}

	/**
	 * @param annotationId
	 *            the annotationId to set
	 */
	public void setAnnotationId(int annotationId) {
		this.annotationId = annotationId;
	}

	/**
	 * @return the accountId
	 */
	public int getAccountId() {
		return accountId;
	}

	/**
	 * @param accountId
	 *            the accountId to set
	 */
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	/**
	 * @return the logId
	 */
	public int getLogId() {
		return logId;
	}

	/**
	 * @param logId
	 *            the logId to set
	 */
	public void setLogId(int logId) {
		this.logId = logId;
	}

	/**
	 * @return the objectId
	 */
	public int getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId
	 *            the objectId to set
	 */
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return the pageNum
	 */
	public int getPageNum() {
		return pageNum;
	}

	/**
	 * @param pageNum
	 *            the pageNum to set
	 */
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @param fullName
	 *            the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the annotationType
	 */
	public String getAnnotationType() {
		return annotationType;
	}

	/**
	 * @param annotationType
	 *            the annotationType to set
	 */
	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}

	/**
	 * @return the naId
	 */
	public String getNaId() {
		return naId;
	}

	/**
	 * @param naId
	 *            the naId to set
	 */
	public void setNaId(String naId) {
		this.naId = naId;
	}

	/**
	 * @return the opaTitle
	 */
	public String getOpaTitle() {
		return opaTitle;
	}

	/**
	 * @param opaTitle
	 *            the opaTitle to set
	 */
	public void setOpaTitle(String opaTitle) {
		this.opaTitle = opaTitle;
	}

	/**
	 * @return the logTs
	 */
	public Timestamp getLogTs() {
		return logTs;
	}

	/**
	 * @param logTs
	 *            the logTs to set
	 */
	public void setLogTs(Timestamp logTs) {
		this.logTs = logTs;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		return aspireContent;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new HashMap<String, Object>();
		return databaseContent;
	}
}
